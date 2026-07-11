local CURRENT_STATION_CODE = 'Ticket-Machine'
local API_BASE = 'http://ticket.fse-media.group/api'
local VERSION = 'v1.5.13.2'
local UPDATE_CHECK_INTERVAL = 5

-- ###########################
-- Core HTTP & JSON Utilities
-- ###########################
local function jsonDecode(str)
  if not str then return nil end
  local ok, res = pcall(textutils.unserializeJSON, str)
  if ok then return res else return nil end
end

local serverConnected = nil
local versionMismatch = nil
local failedUpdateVersion = nil
local pendingMachineUpdate = false
local isUpdating = false
local state = {}

local function normalizeVersionTag(v)
  local s = tostring(v or ''):gsub('^%s+', ''):gsub('%s+$', '')
  if #s == 0 then return '' end
  if s:sub(1, 1):lower() ~= 'v' then
    s = 'v' .. s
  end
  return s:lower()
end

local function setServerConnected(ok)
  if serverConnected == ok then return end
  serverConnected = ok
  serverLastChangeTs = (os.epoch and os.epoch('utc')) or (os.time() * 1000)
  os.queueEvent('config_updated')
end

local function fetchHTTP(url)
  if not http then
    print('Error: HTTP API is disabled in ComputerCraft config!')
    setServerConnected(false)
    return nil
  end

  if http.get then
    local ok, res, err = pcall(http.get, url)
    if not ok or not res then
      if err then print('HTTP GET failed: ' .. tostring(err) .. ' url=' .. tostring(url)) end
      setServerConnected(false)
      return nil
    end
    local data = res.readAll()
    res.close()
    setServerConnected(true)
    return data
  end

  if not http.request then
    print('Error: HTTP API is disabled in ComputerCraft config!')
    return nil
  end

  local okReq, reqOkOrErr = pcall(http.request, { url = url, method = 'GET' })
  if not okReq or not reqOkOrErr then
    print('HTTP request failed: ' .. tostring(reqOkOrErr) .. ' url=' .. tostring(url))
    setServerConnected(false)
    return nil
  end

  while true do
    local ev, u, p1, p2 = os.pullEvent()
    if ev == 'http_success' and u == url then
      local res = p1
      local data = res.readAll()
      res.close()
      setServerConnected(true)
      return data
    elseif ev == 'http_failure' and u == url then
      print('HTTP GET failed: ' .. tostring(p1 or p2 or 'http_failure') .. ' url=' .. tostring(url))
      setServerConnected(false)
      return nil
    else
      os.queueEvent(ev, u, p1, p2)
      sleep(0)
    end
  end
end

local function fetchJSON(url)
  local txt = fetchHTTP(url)
  if not txt then return nil end
  return jsonDecode(txt)
end

local function ensureDir(path)
  local dir = path:match('^(.+)/[^/]+$')
  if dir and not fs.exists(dir) then pcall(fs.makeDir, dir) end
end

local function postJSONResult(url, dataTable)
  if not http then
    setServerConnected(false)
    return false, nil, nil, 'HTTP API disabled'
  end
  local okBody, body = pcall(textutils.serializeJSON, dataTable)
  if not okBody or type(body) ~= 'string' then return false, nil, nil, 'serializeJSON failed' end

  local headers = { ['Content-Type'] = 'application/json' }

  if http.post then
    local ok, res, err = pcall(http.post, url, body, headers)
    if not ok or not res then
      setServerConnected(false)
      return false, nil, nil, tostring(err or res or 'http.post error')
    end
    local code = res.getResponseCode and res.getResponseCode() or nil
    local respBody = res.readAll and res.readAll() or nil
    res.close()
    setServerConnected(true)
    return true, code, respBody, nil
  end

  if not http.request then
    setServerConnected(false)
    return false, nil, nil, 'HTTP API disabled'
  end

  local okReq, reqOkOrErr = pcall(http.request, { url = url, method = 'POST', body = body, headers = headers })
  if not okReq or not reqOkOrErr then
    setServerConnected(false)
    return false, nil, nil, tostring(reqOkOrErr or 'http.request failed')
  end

  while true do
    local ev, u, p1, p2 = os.pullEvent()
    if ev == 'http_success' and u == url then
      local res = p1
      local code = res.getResponseCode and res.getResponseCode() or nil
      local respBody = res.readAll and res.readAll() or nil
      res.close()
      setServerConnected(true)
      return true, code, respBody, nil
    elseif ev == 'http_failure' and u == url then
      setServerConnected(false)
      return false, nil, nil, tostring(p1 or p2 or 'http_failure')
    else
      os.queueEvent(ev, u, p1, p2)
      sleep(0)
    end
  end
end

local function postJSON(url, dataTable)
  local ok, code, body, err = postJSONResult(url, dataTable)
  if not ok or not code then
    return false, code, nil, tostring(err or 'request error')
  end
  local parsed = jsonDecode(body)
  if code < 200 or code >= 300 then
    local msg = nil
    if type(parsed) == 'table' then
      msg = parsed.error or parsed.message or parsed.reason
    end
    if not msg then msg = tostring(body or ('HTTP ' .. tostring(code))) end
    return false, code, parsed, msg
  end
  return true, code, parsed, nil
end

local function unicodeEscape(str)
  if type(str) ~= 'string' then return str end
  local out = {}
  local okUtf8 = type(utf8) == 'table' and type(utf8.codes) == 'function'
  if okUtf8 then
    for _, cp in utf8.codes(str) do
      if cp <= 0x7F then
        out[#out+1] = string.char(cp)
      elseif cp <= 0xFFFF then
        out[#out+1] = string.format("\\u%04x", cp)
      else
        local v = cp - 0x10000
        local hi = 0xD800 + math.floor(v / 0x400)
        local lo = 0xDC00 + (v % 0x400)
        out[#out+1] = string.format("\\u%04x\\u%04x", hi, lo)
      end
    end
    return table.concat(out)
  end
  local i = 1
  while i <= #str do
    local b1 = string.byte(str, i)
    if not b1 then break end
    if b1 <= 0x7F then
      out[#out+1] = string.char(b1)
      i = i + 1
    elseif b1 >= 0xC0 and b1 <= 0xDF then
      local b2 = string.byte(str, i + 1) or 0
      local cp = (b1 % 0x20) * 0x40 + (b2 % 0x40)
      out[#out+1] = string.format("\\u%04x", cp)
      i = i + 2
    elseif b1 >= 0xE0 and b1 <= 0xEF then
      local b2 = string.byte(str, i + 1) or 0
      local b3 = string.byte(str, i + 2) or 0
      local cp = (b1 % 0x10) * 0x1000 + (b2 % 0x40) * 0x40 + (b3 % 0x40)
      out[#out+1] = string.format("\\u%04x", cp)
      i = i + 3
    else
      local b2 = string.byte(str, i + 1) or 0
      local b3 = string.byte(str, i + 2) or 0
      local b4 = string.byte(str, i + 3) or 0
      local cp = (b1 % 0x08) * 0x40000 + (b2 % 0x40) * 0x1000 + (b3 % 0x40) * 0x40 + (b4 % 0x40)
      local v = cp - 0x10000
      local hi = 0xD800 + math.floor(v / 0x400)
      local lo = 0xDC00 + (v % 0x400)
      out[#out+1] = string.format("\\u%04x\\u%04x", hi, lo)
      i = i + 4
    end
  end
  return table.concat(out)
end

local function firstString(...)
  for i = 1, select('#', ...) do
    local v = select(i, ...)
    if v ~= nil then
      local s = tostring(v):gsub('^%s+', ''):gsub('%s+$', '')
      if #s > 0 then return s end
    end
  end
  return ''
end

local function firstNumber(...)
  for i = 1, select('#', ...) do
    local v = select(i, ...)
    if v ~= nil then
      local n = tonumber(v)
      if n ~= nil then return n end
    end
  end
  return nil
end

local function currentDeviceId()
  local label = os.getComputerLabel()
  if label and label ~= "" then
    return label
  end
  return "#" .. tostring(os.getComputerID())
end

local function normalizeTrainTypeLabel(v)
  local s = tostring(v or ''):lower()
  if s == 'express' or s == 'limited_express' or s == 'limited express' then
    return 'Express'
  end
  if s == 'single' then
    return 'Single'
  end
  return 'Local'
end

local PENDING_UPLOAD_PATH = 'logs/pending_ticket_upload.jsonl'
local pendingUploads = {}
local pendingUploadsLoaded = false

local function loadPendingUploadsOnce()
  if pendingUploadsLoaded then return end
  pendingUploadsLoaded = true
  if not fs.exists(PENDING_UPLOAD_PATH) then return end
  for line in io.lines(PENDING_UPLOAD_PATH) do
    local obj = jsonDecode(line)
    if type(obj) == 'table' then table.insert(pendingUploads, obj) end
  end
end

local function savePendingUploads()
  ensureDir(PENDING_UPLOAD_PATH)
  local f = fs.open(PENDING_UPLOAD_PATH, 'w')
  if not f then
    print('Save pending upload failed: cannot open ' .. tostring(PENDING_UPLOAD_PATH))
    return false
  end
  for _, obj in ipairs(pendingUploads) do
    local okSer, s = pcall(textutils.serializeJSON, obj)
    if okSer and type(s) == 'string' then
      f.write(s .. "\n")
    end
  end
  f.close()
  return true
end

local function uploadTicketRecord(ticketData)
  local url = API_BASE .. '/tickets/sale'
  local payload = {
    ticket_id = tostring((type(ticketData) == 'table' and (ticketData.ticket_id or ticketData.id)) or ''),
    start = tostring((type(ticketData) == 'table' and (ticketData.start or ticketData.start_station_id or ticketData.start_station)) or ''),
    terminal = tostring((type(ticketData) == 'table' and (ticketData.terminal or ticketData.terminal_station_id or ticketData.end_station)) or ''),
    train_type = tostring((type(ticketData) == 'table' and (ticketData.train_type or ticketData.trainType or ticketData.type)) or ''),
    cost = (type(ticketData) == 'table' and tonumber(ticketData.cost)) or 0,
    station_code = tostring((type(ticketData) == 'table' and (ticketData.station_code or ticketData.stationCode)) or CURRENT_STATION_CODE or ''),
    device = tostring((type(ticketData) == 'table' and (ticketData.device or ticketData.device_id or ticketData.deviceId)) or 'unknown'),
    trips_total = (type(ticketData) == 'table' and (ticketData.trips_total or ticketData.rides_total or ticketData.rides)) or nil,
    trips_remaining = (type(ticketData) == 'table' and (ticketData.trips_remaining or ticketData.rides_remaining or ticketData.rides)) or nil
  }
  local ok, code, body, err = postJSONResult(url, payload)
  if not ok or not code then
    print('Upload ticket failed: ' .. tostring(err or 'request error') .. ' url=' .. tostring(url))
    return false
  end
  if code < 200 or code >= 300 then
    print('Upload ticket failed: HTTP ' .. tostring(code) .. ' ' .. tostring(body or '') .. ' url=' .. tostring(url))
    return false
  end
  return true
end

local function enqueueTicketUpload(ticketData)
  loadPendingUploadsOnce()
  if uploadTicketRecord(ticketData) then return true end
  table.insert(pendingUploads, ticketData)
  local okSave = savePendingUploads()
  if okSave then
    print('Ticket upload queued. pending=' .. tostring(#pendingUploads))
  else
    print('Ticket upload queued in memory only. pending=' .. tostring(#pendingUploads))
  end
  return false
end

-- ###########################
-- Peripheral discovery
-- ###########################
local SIDE_PRIORITY = { top = 1, bottom = 2, left = 3, right = 4, front = 5, back = 6 }
local REDSTONE_SIDES = { 'right', 'left', 'top', 'bottom', 'front', 'back' }
local monitor = nil
local monitorName = nil
local ticketVendingMachine = nil
local ticketVendingMachineName = nil
local speaker = nil
local speakerName = nil
local detectedPaymentSide = nil
local MOD_DEBUG = true

pcall(math.randomseed, (os.epoch and os.epoch('utc')) or os.time())

local function comparePeripheralName(a, b)
  local pa = SIDE_PRIORITY[tostring(a or '')] or 99
  local pb = SIDE_PRIORITY[tostring(b or '')] or 99
  if pa ~= pb then return pa < pb end
  return tostring(a or '') < tostring(b or '')
end

local function peripheralTypeMatches(name, typeName)
  if not peripheral or type(peripheral.getType) ~= 'function' then return false end
  local got = peripheral.getType(name)
  if type(got) == 'string' then return got == typeName end
  if type(got) == 'table' then
    for _, item in ipairs(got) do
      if item == typeName then return true end
    end
  end
  return false
end

local function findPeripheralByType(typeName)
  if not peripheral then return nil, nil end
  if type(peripheral.getNames) == 'function' and type(peripheral.wrap) == 'function' then
    local names = peripheral.getNames() or {}
    table.sort(names, comparePeripheralName)
    for _, name in ipairs(names) do
      if peripheralTypeMatches(name, typeName) then
        local okWrap, dev = pcall(peripheral.wrap, name)
        if okWrap and type(dev) == 'table' then
          return dev, name
        end
      end
    end
  end
  if type(peripheral.find) == 'function' then
    local dev = peripheral.find(typeName)
    if type(dev) == 'table' then
      local name = nil
      if type(peripheral.getName) == 'function' then
        local okName, gotName = pcall(peripheral.getName, dev)
        if okName then name = gotName end
      end
      return dev, name
    end
  end
  return nil, nil
end

local termDev = term
local w, h = termDev.getSize()

local function refreshDevices()
  local prevSignature = table.concat({
    tostring(monitorName or ''),
    tostring(ticketVendingMachineName or ''),
    tostring(speakerName or '')
  }, '|')
  monitor, monitorName = findPeripheralByType('monitor')
  ticketVendingMachine, ticketVendingMachineName = findPeripheralByType('ticket_vending_machine')
  speaker, speakerName = findPeripheralByType('speaker')
  termDev = monitor or term
  if monitor then pcall(monitor.setTextScale, 0.5) end
  w, h = termDev.getSize()
  local nextSignature = table.concat({
    tostring(monitorName or ''),
    tostring(ticketVendingMachineName or ''),
    tostring(speakerName or '')
  }, '|')
  if prevSignature ~= nextSignature then
    os.queueEvent('config_updated')
  end
end

refreshDevices()

local function saveCardIssueSnapshot(cardData)
  pcall(function()
    ensureDir('logs/last_card_issue.json')
    local okSer, s = pcall(textutils.serializeJSON, cardData)
    if okSer and type(s) == 'string' then
      local f = fs.open('logs/last_card_issue.json', 'w')
      if f then f.write(s); f.close() end
    end
  end)
end

local function extractPeripheralId(...)
  for i = 1, select('#', ...) do
    local v = select(i, ...)
    if type(v) == 'string' or type(v) == 'number' then
      local s = tostring(v):gsub('^%s+', ''):gsub('%s+$', '')
      if #s > 0 then return s end
    end
  end
  return ''
end

local function peripheralCallSucceeded(r1)
  return r1 ~= nil and r1 ~= false
end

local function getTicketVendingMachine()
  refreshDevices()
  if type(ticketVendingMachine) == 'table' then
    return ticketVendingMachine
  end
  return nil
end

local function callPeripheralMethods(dev, methodNames, variants)
  if type(dev) ~= 'table' then return false, 'peripheral_unavailable' end
  for _, methodName in ipairs(methodNames) do
    local fn = dev[methodName]
    if type(fn) == 'function' then
      for _, args in ipairs(variants) do
        local okCall, r1, r2, r3 = pcall(fn, table.unpack(args))
        if okCall and peripheralCallSucceeded(r1) then
          return true, methodName, r1, r2, r3
        end
      end
    end
  end
  return false, 'unsupported_method'
end

local function issueBlankICCard(holderName, initialBalance)
  local dev = getTicketVendingMachine()
  if type(dev) ~= 'table' then return false, '', 'peripheral_unavailable' end
  local safeHolderName = firstString(holderName, 'CARD USER')
  local safeInitialBalance = math.max(0, math.floor(tonumber(initialBalance) or 0))
  for _, methodName in ipairs({ 'issueICCard', 'issueCard' }) do
    local fn = dev[methodName]
    if type(fn) == 'function' then
      local okCall, r1, r2, r3 = pcall(fn, safeHolderName, safeInitialBalance)
      if not (okCall and peripheralCallSucceeded(r1)) then
        okCall, r1, r2, r3 = pcall(fn, safeHolderName)
      end
      if not (okCall and peripheralCallSucceeded(r1)) then
        okCall, r1, r2, r3 = pcall(fn)
      end
      if okCall and peripheralCallSucceeded(r1) then
        return true, extractPeripheralId(r1, r2, r3), methodName
      end
    end
  end
  return false, '', 'unsupported_method'
end

local function writeICCard(cardData, opts)
  local dev = getTicketVendingMachine()
  local options = opts or {}
  local payload = {}
  for k, v in pairs(cardData or {}) do payload[k] = v end
  payload.media = payload.media or 'ic_card'
  payload.product_type = payload.product_type or 'stored_value'
  payload.device = payload.device or currentDeviceId()
  payload.initial_balance = payload.initial_balance or payload.topup or payload.balance or 0
  payload.owner = payload.owner or payload.holder_name or ''
  payload.card_holder = payload.card_holder or payload.holder_name or ''
  _G.TICKET_MACHINE_LAST_TICKET = payload
  saveCardIssueSnapshot(payload)

  local okWrite, methodName, r1, r2, r3 = callPeripheralMethods(dev,
    options.writeOnly
      and { 'writeCard', 'writeICCard', 'writeTicketData', 'issueTicketData' }
      or { 'issueCard', 'writeCard', 'writeICCard', 'issueTicketData', 'writeTicketData', 'issueICCard' },
    {
      { payload },
      { tostring(payload.holder_name or ''), tonumber(payload.initial_balance) or 0 },
      { tostring(payload.holder_name or '') },
      { tostring(payload.card_id or ''), tostring(payload.holder_name or ''), tonumber(payload.balance) or 0, tonumber(payload.deposit) or 0, tostring(payload.station_code or ''), tostring(payload.product_type or 'stored_value') },
      { tostring(payload.holder_name or ''), tonumber(payload.balance) or 0, tonumber(payload.deposit) or 0 },
      {}
    })
  if okWrite then
    local issuedId = extractPeripheralId(r1, r2, r3, payload.card_id)
    if #issuedId > 0 then payload.card_id = issuedId end
    return true, payload, methodName
  end
  return false, payload, methodName
end

local function submitCardOpen(payload)
  return postJSON(API_BASE .. '/cards/open', payload)
end

local function buildFinalCardData(payload, respData)
  local data = (type(respData) == 'table') and respData or {}
  return {
    card_id = firstString(data.card_id, data.id, payload.card_id),
    holder_name = payload.holder_name,
    balance = firstNumber(data.balance, data.stored_value, payload.balance) or payload.balance,
    deposit = firstNumber(data.deposit, payload.deposit) or payload.deposit,
    topup = firstNumber(data.topup, data.first_topup, payload.topup) or payload.topup,
    station_code = payload.station_code,
    device = payload.device,
    voucher_code = payload.voucher_code,
    media = 'ic_card',
    product_type = 'stored_value',
    order_value = payload.order_value,
    initial_balance = firstNumber(data.first_topup, data.topup, payload.topup, payload.balance) or payload.topup or payload.balance or 0
  }
end

local function generateNumericCode(prefix, digits)
  local width = math.max(1, math.floor(tonumber(digits) or 1))
  local maxValue = (10 ^ width) - 1
  local num = string.format('%0' .. tostring(width) .. 'd', math.random(0, maxValue))
  return tostring(prefix or 'ID'):upper() .. '-' .. num
end

local function generateCardId()
  return generateNumericCode('IC', 6)
end

local function issueTicketFromPeripheral(fromNameEnArg, toNameEnArg, apiType, rides, cost, startStationArg, terminalStationArg, fromNameCnUArg, toNameCnUArg, fallbackTicketId)
  local dev = getTicketVendingMachine()
  if type(dev) ~= 'table' then
    return false, '', 'peripheral_unavailable'
  end
  local fn = dev.issueTicket
  if type(fn) ~= 'function' then
    return false, '', 'unsupported_method'
  end

  local function normalizeIssuedTicketId(id)
    if id == nil then return '' end
    local s = tostring(id):gsub('%s+', '')
    if #s == 0 then return '' end
    local prefix, num = s:match('^([A-Za-z][A-Za-z])%-?([0-9]+)$')
    if prefix and num then
      prefix = prefix:upper()
      if #num < 8 then
        num = string.rep('0', 8 - #num) .. num
      end
      return prefix .. '-' .. num
    end
    return s
  end

  local function tryIssue(...)
    local okCall, r1, r2, r3 = pcall(fn, ...)
    if not (okCall and peripheralCallSucceeded(r1)) then
      return false, '', okCall and 'issue_failed' or 'issue_call_failed'
    end
    local issuedId = extractPeripheralId(r2, r3, r1, fallbackTicketId)
    local normalizedId = normalizeIssuedTicketId(issuedId)
    if #normalizedId == 0 then
      return false, '', 'invalid_ticket_id'
    end
    return true, normalizedId, 'issueTicket'
  end

  local okIssue, ticketId, issueErr = tryIssue(fromNameEnArg, toNameEnArg, apiType, rides, cost, startStationArg, terminalStationArg, fromNameCnUArg, toNameCnUArg)
  if okIssue then
    return true, ticketId, 'issueTicket'
  end

  return tryIssue(fromNameEnArg, toNameEnArg, apiType, rides, cost, startStationArg, terminalStationArg)
end

-- ###########################
-- Audio Utilities & Playback
-- ###########################
local function stopAudio() end

local function waitAudioComplete() end

local function playNote(instrument, pitch, volume, dur)
  if not speaker then return end
  instrument = tostring(instrument or 'pling')
  local p = tonumber(pitch)
  if p == nil then p = 12 end
  p = math.max(0, math.min(24, math.floor(p)))
  local v = tonumber(volume)
  if v == nil then v = 1 end
  if v < 0 then v = 0 end
  local ok = pcall(function() speaker.playNote(instrument, v, p) end)
  if ok and dur and dur > 0 then sleep(dur) end
end

local function playMelody(instrument, notes, volume, noteDur, gap)
  if not speaker then return end
  instrument = tostring(instrument or 'harp')
  local v = tonumber(volume); if v == nil then v = 1 end
  local nd = tonumber(noteDur); if nd == nil then nd = 0.04 end
  local g = tonumber(gap); if g == nil then g = 0.02 end
  if type(notes) ~= 'table' then return end
  for i = 1, #notes do
    playNote(instrument, notes[i], v, nd)
    if g > 0 then sleep(g) end
  end
end


local function playConfirmTicketMelody()
  playMelody('bell', { 19, 22, 24, 22, 19 }, 1, 0.04, 0.02)
end

local function playAudioFile(path)
  local p = tostring(path or ''):lower()
  if p:find('welcome') then
    playMelody('chime', { 12, 16, 19, 24 }, 1, 0.05, 0.02)
    return
  end
end

local function clickSound() end


-- ###########################
-- Background Tasks
-- ###########################
local function getLastUpdateVersion()
  if not fs.exists(".last_update_version") then return nil end
  local f = fs.open(".last_update_version", "r")
  if not f then return nil end
  local v = f.readAll()
  f.close()
  return v
end

local function setLastUpdateVersion(v)
  local f = fs.open(".last_update_version", "w")
  if f then f.write(tostring(v)); f.close() end
end

local function runSilentMachineUpdate()
  if not fs.exists('update_machine.lua') then
    return false, "update_machine.lua not found"
  end
  local baseEnv = (getfenv and getfenv()) or _ENV or _G
  local env = setmetatable({ AUTO_UPDATE_SILENT = true }, { __index = baseEnv })
  local fn, err = loadfile('update_machine.lua', env)
  if not fn then return false, err end
  local ok, res = pcall(fn, '--silent')
  if not ok then return false, tostring(res) end
  return true, true
end

local lastUpdateError = nil

local function drawVersionIndicator()
  if w < 1 then return end
  local markerColor = colors.yellow
  local markerText = '*   '
  if isUpdating then
    markerColor = colors.yellow
    markerText = '^ing'
  elseif versionMismatch == true then
    markerColor = colors.red
    markerText = '*   '
  elseif versionMismatch == false then
    markerColor = colors.lime
    markerText = '    '
  end
  termDev.setBackgroundColor(colors.black)
  termDev.setTextColor(colors.gray)
  termDev.setCursorPos(1, 1)
  termDev.write(tostring(VERSION))
  termDev.setTextColor(markerColor)
  termDev.write(markerText)
  
  if lastUpdateError then
    termDev.setCursorPos(1, 2)
    termDev.setTextColor(colors.red)
    termDev.write(string.sub(tostring(lastUpdateError), 1, w))
  end
  
  termDev.setTextColor(colors.white)
end

local function backgroundSyncTask()
  pcall(refreshConfigOnce)
  while true do
    if type(CFG) == 'table' and type(CFG.force_update) == 'boolean' and CFG.force_update == true then
      local expected = CFG.lua_versions and CFG.lua_versions.ticketmachine
      if expected and expected ~= getLastUpdateVersion() and expected ~= failedUpdateVersion then
        pendingMachineUpdate = true
      end
    end

    if pendingMachineUpdate then
      isUpdating = true
      lastUpdateError = nil
      pcall(drawVersionIndicator)
      sleep(0.5)
      local ok, updated = pcall(runSilentMachineUpdate)
      if ok and updated == true then
        local expected = CFG.lua_versions and CFG.lua_versions.ticketmachine   
        if expected then setLastUpdateVersion(expected) end
        os.reboot()
        return
      end
      isUpdating = false
      pendingMachineUpdate = false
      lastUpdateError = tostring(updated)
      failedUpdateVersion = CFG.lua_versions and CFG.lua_versions.ticketmachine
      pcall(drawVersionIndicator)
    end
    sleep(UPDATE_CHECK_INTERVAL)
    pcall(refreshConfigOnce)
  end
end

local function backgroundPeripheralTask()
  while true do
    local ev = os.pullEvent()
    if ev == 'peripheral' or ev == 'peripheral_detach' then
      pcall(refreshDevices)
    end
  end
end

local function backgroundTicketUploadTask()
  loadPendingUploadsOnce()
  local backoff = 2
  while true do
    if #pendingUploads > 0 then
      if uploadTicketRecord(pendingUploads[1]) then
        table.remove(pendingUploads, 1)
        savePendingUploads()
        backoff = 2
      else
        backoff = math.min(60, math.floor(backoff * 1.5))
      end
    end
    sleep(backoff)
  end
end



-- ###########################
-- Data logic & Config
-- ###########################
local CFG = { stations = {}, lines = {}, fares = {}, transfers = {} }
local stationByCode = {}
local adjacency_regular, adjacency_express = {}, {}
local transferGroupByCode = {}

local function updateVersionStateFromConfig()
  local remote = normalizeVersionTag(type(CFG.lua_versions) == 'table' and CFG.lua_versions.ticketmachine or nil)
  if #remote == 0 then
    expectedMachineVersion = nil
    versionMismatch = nil
    return
  end
  expectedMachineVersion = remote
  versionMismatch = (remote ~= normalizeVersionTag(VERSION))
end

local function normalizeCode(s)
  s = tostring(s or '')
  s = s:gsub('[\239\187\191]', ''):gsub('%s+', '')
  return s
end

local function rebuildMaps()
  stationByCode = {}
  for _, s in ipairs(CFG.stations or {}) do
    if type(s) == 'table' then
      local code = normalizeCode(s.code or s.id)
      if #code > 0 then
        s.code = code
        if s.en_name == nil then s.en_name = s.en or s.enName or s.name_en or s.english_name end
        stationByCode[code] = s
      end
    end
  end
  adjacency_regular, adjacency_express = {}, {}
  local transferAdj = {}
  local function addTransfer(a, b)
    a = normalizeCode(a)
    b = normalizeCode(b)
    if #a == 0 or #b == 0 or a == b then return end
    adjacency_regular[a] = adjacency_regular[a] or {}; adjacency_regular[a][b] = 0
    adjacency_regular[b] = adjacency_regular[b] or {}; adjacency_regular[b][a] = 0
    adjacency_express[a] = adjacency_express[a] or {}; adjacency_express[a][b] = 0
    adjacency_express[b] = adjacency_express[b] or {}; adjacency_express[b][a] = 0
    transferAdj[a] = transferAdj[a] or {}; transferAdj[a][b] = true
    transferAdj[b] = transferAdj[b] or {}; transferAdj[b][a] = true
  end
  for _, e in ipairs(CFG.fares or {}) do
    local cr = e.cost_regular or e.cost or 0
    local ce = e.cost_express or e.cost or 0
    adjacency_regular[e.from] = adjacency_regular[e.from] or {}
    adjacency_regular[e.from][e.to] = cr
    adjacency_regular[e.to] = adjacency_regular[e.to] or {}
    adjacency_regular[e.to][e.from] = cr
    adjacency_express[e.from] = adjacency_express[e.from] or {}
    adjacency_express[e.from][e.to] = ce
    adjacency_express[e.to] = adjacency_express[e.to] or {}
    adjacency_express[e.to][e.from] = ce
  end
  for _, p in ipairs(CFG.transfers or {}) do
    addTransfer(p[1], p[2])
  end
  for _, s in ipairs(CFG.stations or {}) do
    if type(s) == 'table' and s.transfer_enabled and type(s.transfer_to) == 'table' then
      local from = normalizeCode(s.code or s.id)
      for _, t in ipairs(s.transfer_to) do
        local to = t
        if type(t) == 'table' then to = t.code or t.station or t.id or t[1] end
        addTransfer(from, to)
      end
    end
  end
  local groups = {}
  for _, s in ipairs(CFG.stations or {}) do
    if type(s) == 'table' then
      local cn = tostring(s.name or s.cn_name or ''):gsub('%s+', '')
      local en = tostring(s.en_name or s.en or s.enName or ''):lower():gsub('%s+', '')
      local code = normalizeCode(s.code or s.id)
      if #cn > 0 and #en > 0 and #code > 0 then
        local k = cn .. '|' .. en
        groups[k] = groups[k] or {}
        table.insert(groups[k], code)
      end
    end
  end
  for _, arr in pairs(groups) do
    if #arr >= 2 then
      for i = 1, #arr do
        for j = i + 1, #arr do
          local a, b = tostring(arr[i] or ''), tostring(arr[j] or '')
          if #a > 0 and #b > 0 and a ~= b then
            addTransfer(a, b)
          end
        end
      end
    end
  end
  transferGroupByCode = {}
  local visited = {}
  local groupId = 0
  local function flood(start)
    groupId = groupId + 1
    local q = { start }
    visited[start] = true
    while #q > 0 do
      local u = table.remove(q, 1)
      transferGroupByCode[u] = groupId
      for v, _ in pairs(transferAdj[u] or {}) do
        if not visited[v] then
          visited[v] = true
          table.insert(q, v)
        end
      end
    end
  end
  for code, _ in pairs(stationByCode) do
    if not visited[code] then flood(code) end
  end
  for code, _ in pairs(transferAdj) do
    if not visited[code] then flood(code) end
  end
end

local function sameLogicalStation(a, b)
  a = normalizeCode(a)
  b = normalizeCode(b)
  if a == b then return true end
  local ga = transferGroupByCode[a]
  local gb = transferGroupByCode[b]
  return ga ~= nil and ga == gb
end

local function fetchConfig()
  local txt = fetchHTTP(API_BASE .. '/config')
  return txt and jsonDecode(txt) or nil
end

local function refreshConfigOnce()
  local cfg = fetchConfig()
  if not cfg then return false end
  local f = fs.open('config.json', 'w')
  if f then f.write(textutils.serializeJSON(cfg)); f.close() end
  CFG = cfg
  rebuildMaps()
  updateVersionStateFromConfig()
  os.queueEvent('config_updated')
  return true
end

local function loadConfig()
  local cfg = fetchConfig()
  if cfg then
    local f = fs.open('config.json', 'w')
    if f then f.write(textutils.serializeJSON(cfg)); f.close() end
    return cfg
  end
  if fs.exists('config.json') then
    local f = fs.open('config.json', 'r')
    local c = f.readAll(); f.close()
    return jsonDecode(c)
  end
  return nil
end

CFG = loadConfig() or CFG
rebuildMaps()
updateVersionStateFromConfig()


-- ###########################
-- UI Helpers
-- ###########################
local CC_PALETTE = {
  {name='white', val=colors.white, rgb={0xF2,0xF2,0xF2}},
  {name='orange', val=colors.orange, rgb={0xF2,0xB2,0x33}},
  {name='magenta', val=colors.magenta, rgb={0xE5,0x7F,0xD8}},
  {name='lightBlue', val=colors.lightBlue, rgb={0x99,0xB2,0xF2}},
  {name='yellow', val=colors.yellow, rgb={0xDE,0xDE,0x6C}},
  {name='lime', val=colors.lime, rgb={0x7F,0xCC,0x19}},
  {name='pink', val=colors.pink, rgb={0xF2,0xB2,0xCC}},
  {name='gray', val=colors.gray, rgb={0x4C,0x4C,0x4C}},
  {name='lightGray', val=colors.lightGray, rgb={0x99,0x99,0x99}},
  {name='cyan', val=colors.cyan, rgb={0x4C,0x99,0xB2}},
  {name='purple', val=colors.purple, rgb={0xB2,0x66,0xE5}},
  {name='blue', val=colors.blue, rgb={0x33,0x66,0xCC}},
  {name='brown', val=colors.brown, rgb={0x7F,0x66,0x4C}},
  {name='green', val=colors.green, rgb={0x57,0xA6,0x4E}},
  {name='red', val=colors.red, rgb={0xCC,0x4C,0x4C}},
  {name='black', val=colors.black, rgb={0x11,0x11,0x11}},
}

local function nearestCCColor(val)
  if type(val) ~= 'string' then return colors.gray end
  if val:sub(1,1) == '#' then
    local hex = val:sub(2)
    local r, g, b = tonumber(hex:sub(1,2), 16), tonumber(hex:sub(3,4), 16), tonumber(hex:sub(5,6), 16)
    if not r then return colors.gray end
    local bestD, bestV = math.huge, colors.gray
    for _,c in ipairs(CC_PALETTE) do
      local d = (r-c.rgb[1])^2 + (g-c.rgb[2])^2 + (b-c.rgb[3])^2
      if d < bestD then bestD, bestV = d, c.val end
    end
    return bestV
  end
  for _,c in ipairs(CC_PALETTE) do if c.name == val then return c.val end end
  return colors.gray
end

local function clear()
  termDev.setBackgroundColor(colors.black)
  termDev.clear()
  termDev.setCursorPos(1,1)
end

local function centerText(y, text, color)
  termDev.setTextColor(color or colors.white)
  local x = math.max(1, math.floor((w - #text) / 2))
  termDev.setCursorPos(x, y)
  termDev.write(text)
end

local function drawRainbowLabelRow(y, text, fg)
  local palette = { colors.red, colors.orange, colors.yellow, colors.lime, colors.green, colors.cyan, colors.blue, colors.purple, colors.magenta }
  local x = math.max(1, math.floor((w - #text) / 2))
  termDev.setTextColor(fg or colors.white)
  for i = 1, #text do
    termDev.setBackgroundColor(palette[((i - 1) % #palette) + 1])
    termDev.setCursorPos(x + i - 1, y)
    termDev.write(text:sub(i, i))
  end
  termDev.setBackgroundColor(colors.black)
end

local Buttons = {}
local function addButton(x, y, label, wBtn, hBtn, colorsPair, onClick)
  table.insert(Buttons, { x=x, y=y, w=wBtn, h=hBtn, onClick=onClick })
  termDev.setBackgroundColor(colorsPair[1]); termDev.setTextColor(colorsPair[2])
  for i=0,hBtn-1 do termDev.setCursorPos(x, y+i); termDev.write(string.rep(' ', wBtn)) end
  termDev.setCursorPos(x + math.floor((wBtn - #label)/2), y + math.floor(hBtn/2))
  termDev.write(label)
end

local function inRect(btn, px, py)
  return px >= btn.x and px <= (btn.x + btn.w - 1) and py >= btn.y and py <= (btn.y + btn.h - 1)
end

local function waitButtons()
  while true do
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == 'mouse_click' or ev == 'monitor_touch' then
      for _, b in ipairs(Buttons) do
        if inRect(b, p2, p3) then 
          clickSound(); if b.onClick then b.onClick() end; return 
        end
      end
    elseif ev == 'trigger_update' then
      pendingMachineUpdate = true
    elseif ev == 'config_updated' then
      if state and state.page == 'home' then return end
    end
  end
end

-- ###########################
-- Pages & Navigation
-- ###########################
state = {
  page = 'home',
  stationName = (CFG.current_station and (CFG.current_station.en_name or CFG.current_station.name)) or 'Station',
  stationCode = (CFG.current_station and CFG.current_station.code) or CURRENT_STATION_CODE,
  departure = nil, terminal = nil, trainType = nil, trips = 1, cost = 0, paid = 0, doneAudioPlayed = false,
  productMode = 'ticket', cardMode = nil, cardPaymentMode = 'local',
  holderName = '', cardDeposit = 0, cardTopup = 0, cardBalance = 0, cardOrderValue = 0,
  card_id = nil, card_server_data = nil, pendingBlankCardId = nil
}

local function getCardConfig()
  local cfg = CFG.card or CFG.ic_card or {}
  local minTopup = math.max(1, math.floor(firstNumber(cfg.first_topup_min, cfg.min_first_topup, cfg.min_topup, 10) or 10))
  local quickSrc = type(cfg.quick_amounts) == 'table' and cfg.quick_amounts or { 10, 20, 50, 100 }
  local quick = {}
  for _, v in ipairs(quickSrc) do
    local n = tonumber(v)
    if n and n > 0 then quick[#quick + 1] = math.floor(n) end
  end
  if #quick == 0 then quick = { 10, 20, 50, 100 } end
  return {
    deposit = 0,
    min_topup = minTopup,
    quick_amounts = quick
  }
end

local function resetTicketFlow()
  state.productMode = 'ticket'
  state.cardMode = nil
  state.cardPaymentMode = 'local'
  state.departure, state.terminal, state.trainType = nil, nil, nil
  state.trips, state.cost, state.paid, state.doneAudioPlayed = 1, 0, 0, false
  state.voucher_code = nil
  state.holderName = ''
  state.cardDeposit = 0
  state.cardTopup = 0
  state.cardBalance = 0
  state.cardOrderValue = 0
  state.card_id = nil
  state.card_server_data = nil
  state.pendingBlankCardId = nil
end

local function resetCardFlow(mode)
  local cardCfg = getCardConfig()
  state.productMode = 'card'
  state.cardMode = mode or 'open'
  state.cardPaymentMode = 'local'
  state.departure, state.terminal, state.trainType = nil, nil, nil
  state.trips, state.paid, state.doneAudioPlayed = 1, 0, 0, false
  state.voucher_code = nil
  state.holderName = ''
  state.cardDeposit = cardCfg.deposit
  state.cardTopup = cardCfg.min_topup
  state.cardBalance = cardCfg.min_topup
  state.cardOrderValue = state.cardTopup
  state.cost = state.cardOrderValue
  state.card_id = nil
  state.card_server_data = nil
  state.pendingBlankCardId = nil
end

local function isCardOrderLike(data)
  if type(data) ~= 'table' then return false end
  local kind = firstString(
    data.order_type, data.type, data.kind, data.media, data.media_type, data.product,
    data.product_type, data.ticket_type, data.service_type
  ):lower()
  if kind:find('card', 1, true) or kind:find('stored', 1, true) or kind:find('wallet', 1, true) then
    return true
  end
  if kind:find('ic', 1, true) then return true end
  if type(data.card) == 'table' or type(data.ic_card) == 'table' then return true end
  return firstString(data.holder_name, data.holderName, data.passenger_name, data.passengerName) ~= ''
end

local function buildCardOrderState(data, voucherCode)
  if not isCardOrderLike(data) then return nil end
  local cardCfg = getCardConfig()
  local totalValue = math.max(0, math.floor(firstNumber(
    data.order_value, data.total_value, data.price, data.cost, data.amount,
    type(data.card) == 'table' and data.card.order_value or nil,
    type(data.ic_card) == 'table' and data.ic_card.order_value or nil,
    cardCfg.min_topup
  ) or cardCfg.min_topup))
  local topup = math.max(0, math.floor(firstNumber(
    data.topup, data.first_topup, data.recharge, data.initial_balance,
    type(data.card) == 'table' and data.card.topup or nil,
    type(data.ic_card) == 'table' and data.ic_card.topup or nil,
    totalValue
  ) or totalValue))
  local balance = math.max(0, math.floor(firstNumber(
    data.balance, data.stored_value, data.wallet_balance,
    type(data.card) == 'table' and data.card.balance or nil,
    type(data.ic_card) == 'table' and data.ic_card.balance or nil,
    topup
  ) or topup))
  return {
    holderName = firstString(
      data.holder_name, data.holderName, data.passenger_name, data.passengerName,
      type(data.card) == 'table' and data.card.holder_name or nil,
      type(data.ic_card) == 'table' and data.ic_card.holder_name or nil,
      'CARD USER'
    ),
    deposit = 0,
    topup = topup,
    balance = balance,
    orderValue = totalValue,
    voucher = voucherCode,
    raw = data
  }
end

local function stationDisplay(code)
  code = normalizeCode(code)
  if #code == 0 then return '' end
  if code == 'SINGLE_SEGMENT' then return 'Any 1 Segment' end
  local st = stationByCode[code]
  if st then return tostring(st.en_name or st.en or st.enName or st.name or code) .. ' ' .. code end
  return code
end

local function drawServerStatusIndicator()
  if w < 2 then return end
  local col = colors.yellow
  if serverConnected == true then col = colors.lime
  elseif serverConnected == false then col = colors.red end
  termDev.setBackgroundColor(colors.black)
  termDev.setTextColor(col)
  termDev.setCursorPos(w-1, 1)
  termDev.write('S')
  termDev.setCursorPos(w, 1)
  termDev.write('*')
  termDev.setTextColor(colors.white)
end

local function drawHeader(title, sub, hideStationLabel)
  clear()
  drawVersionIndicator()
  drawServerStatusIndicator()
  centerText(2, title, colors.white)
  if sub and #tostring(sub) > 0 then centerText(3, tostring(sub), colors.lightBlue) end
end

local ui_cancel_request, ui_cancel_confirmed = false, false
local function renderConfirmCancel()
  local boxW, boxH = math.max(24, math.min(32, w-4)), 6
  local bx, by = math.floor((w-boxW)/2)+1, math.floor((h-boxH)/2)+1
  termDev.setBackgroundColor(colors.gray)
  for i=0,boxH-1 do termDev.setCursorPos(bx, by+i); termDev.write(string.rep(' ', boxW)) end
  local msg = 'Cancel this purchase?'
  if #msg > boxW-4 then msg = msg:sub(1, boxW-4) end
  termDev.setTextColor(colors.white); termDev.setCursorPos(bx+2, by+2); termDev.write(msg)
  Buttons = {}
  local bw = math.floor((boxW - 6) / 2)
  addButton(bx+2, by+4, 'YES', bw, 1, {colors.red, colors.white}, function() ui_cancel_confirmed = true; ui_cancel_request = false end)
  addButton(bx+4+bw, by+4, 'NO', bw, 1, {colors.green, colors.white}, function() ui_cancel_confirmed = false; ui_cancel_request = false end)
end

local function showAlert(msg)
  local boxW, boxH = math.max(24, math.min(32, w-4)), 6
  local bx, by = math.floor((w-boxW)/2)+1, math.floor((h-boxH)/2)+1
  termDev.setBackgroundColor(colors.gray)
  for i=0,boxH-1 do termDev.setCursorPos(bx, by+i); termDev.write(string.rep(' ', boxW)) end
  msg = tostring(msg or '')
  if #msg > boxW-4 then msg = msg:sub(1, boxW-4) end
  termDev.setTextColor(colors.white); termDev.setCursorPos(bx+2, by+2); termDev.write(msg)
  Buttons = {}
  addButton(bx+math.floor((boxW-6)/2), by+4, 'OK', 6, 1, {colors.green, colors.white}, function() end)
  waitButtons()
end

local function addCancelButton()
  addButton(2, h, 'CANCEL', 8, 1, {colors.red, colors.white}, function() ui_cancel_request = true end)
end

local singleSegments = nil
local singleSegmentScroll = 0

local function buildSingleSegments()
  local segments = {}
  local fares = CFG.fares or {}
  for _, line in ipairs(CFG.lines or {}) do
    local stops = {}
    if type(line.stops) == 'table' then stops = line.stops
    elseif type(line['站点列表']) == 'table' then stops = line['站点列表']
    elseif type(line.stations) == 'table' then stops = line.stations
    elseif type(line['站点']) == 'table' then stops = line['站点']
    end
    local lineFares = {}
    for i = 1, #stops - 1 do
      local a, b = normalizeCode(stops[i]), normalizeCode(stops[i+1])
      local f = nil
      for _, fx in ipairs(fares) do
        local from = normalizeCode(fx.from)
        local to = normalizeCode(fx.to)
        if (from == a and to == b) or (from == b and to == a) then
          f = fx
          break
        end
      end
      if f then
        local cost = tonumber(f.cost_regular or f.cost) or 0
        lineFares[cost] = true
      end
    end
    for cost, _ in pairs(lineFares) do
      table.insert(segments, {
        lineName = line.en_name or line.enName or line.en or line.name or line['线路名称'] or 'Unknown',
        color = line.color or line['颜色'],
        cost = cost
      })
    end
  end
  return segments
end

local function showSingleSegmentSelection()
  singleSegments = buildSingleSegments()
  singleSegmentScroll = 0
  while state.page == 'single_segment' do
    drawHeader('Select Single Segment', 'Select a line and fare')
    Buttons = {}
    local itemsPerPage = math.floor((h - 10) / 4) * 2
    if itemsPerPage < 2 then itemsPerPage = 2 end
    local maxScroll = math.ceil(#singleSegments / itemsPerPage) - 1
    if maxScroll < 0 then maxScroll = 0 end
    if singleSegmentScroll > maxScroll then singleSegmentScroll = maxScroll end
    if singleSegmentScroll < 0 then singleSegmentScroll = 0 end
    
    local startIdx = singleSegmentScroll * itemsPerPage + 1
    local endIdx = math.min(startIdx + itemsPerPage - 1, #singleSegments)
    
    local bw = math.floor((w - 6) / 2)
    local bx1, bx2 = 2, w - bw - 1
    local by = 5
    for i = startIdx, endIdx do
      local seg = singleSegments[i]
      local colIdx = (i - startIdx) % 2
      local rowIdx = math.floor((i - startIdx) / 2)
      local x = colIdx == 0 and bx1 or bx2
      local y = by + rowIdx * 4
      
      local label = seg.lineName .. ' cogs:' .. tostring(seg.cost)
      if #label > bw then label = label:sub(1, bw) end
      local isSelected = (state.singleSegment == seg)
      addButton(x, y, label, bw, 3, {isSelected and colors.green or colors.blue, colors.white}, function()
        state.singleSegment = seg
      end)
    end
    
    if singleSegmentScroll > 0 then
      addButton(w-8, 2, 'UP', 6, 1, {colors.gray, colors.white}, function() singleSegmentScroll = singleSegmentScroll - 1 end)
    end
    if singleSegmentScroll < maxScroll then
      addButton(w-8, h-5, 'DN', 6, 1, {colors.gray, colors.white}, function() singleSegmentScroll = singleSegmentScroll + 1 end)
    end
    
    addCancelButton()
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'home' end)
    if state.singleSegment then
      addButton(w-9, h-3, 'NEXT->', 8, 3, {colors.black, colors.green}, function() 
        state.departure = state.singleSegment.lineName
        state.terminal = 'SINGLE_SEGMENT'
        state.trainType = 'single'
        state.trips = 1
        state.cost = state.singleSegment.cost
        state.page = 'order' 
      end)
    end
    waitButtons()
    if ui_cancel_request then
      renderConfirmCancel()
      waitButtons()
      if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false end
      ui_cancel_request = false
    end
  end
end

local function showHome()
  state.stationName = (CFG.current_station and (CFG.current_station.en_name or CFG.current_station.en or CFG.current_station.enName or CFG.current_station.name)) or 'Station'
  state.stationCode = (CFG.current_station and CFG.current_station.code) or CURRENT_STATION_CODE
  drawHeader('FSE Ticket Machine', 'Select Mode', true)
  Buttons = {}
  local btnW, btnH = 12, 3
  local y = math.floor(h/2) - 3
  local x1, x2, x3 = math.floor(w/2) - btnW - 2, math.floor(w/2) + 3, math.floor(w/2) - math.floor(btnW / 2)
  if x1 < 2 or (x2 + btnW) > w-1 then
    local cx = math.floor((w - btnW) / 2) + 1
    addButton(cx, y-4, 'NEW', btnW, btnH, {colors.green, colors.white}, function()
      resetTicketFlow()
      stopAudio(); playAudioFile('Audio/welcome.wav'); waitAudioComplete()
      state.page = 'departure'
    end)
    addButton(cx, y, 'SINGLE', btnW, btnH, {colors.blue, colors.white}, function()
      resetTicketFlow()
      stopAudio(); playAudioFile('Audio/welcome.wav'); waitAudioComplete()
      state.page = 'single_segment'
    end)
    addButton(cx, y+4, 'CARD', btnW, btnH, {colors.orange, colors.white}, function()
      resetCardFlow('open')
      stopAudio(); state.page = 'card_home'
    end)
    addButton(cx, y+8, 'ONLINE', btnW, btnH, {colors.cyan, colors.white}, function()
      resetTicketFlow()
      stopAudio(); state.page = 'online'
    end)
  else
    local cx = math.floor((w - btnW) / 2) + 1
    addButton(x1, y-2, 'NEW', btnW, btnH, {colors.green, colors.white}, function()
      resetTicketFlow()
      stopAudio(); playAudioFile('Audio/welcome.wav'); waitAudioComplete()
      state.page = 'departure'
    end)
    addButton(x2, y-2, 'SINGLE', btnW, btnH, {colors.blue, colors.white}, function()
      resetTicketFlow()
      stopAudio(); playAudioFile('Audio/welcome.wav'); waitAudioComplete()
      state.page = 'single_segment'
    end)
    addButton(x1, y+2, 'CARD', btnW, btnH, {colors.orange, colors.white}, function()
      resetCardFlow('open')
      stopAudio(); state.page = 'card_home'
    end)
    addButton(x2, y+2, 'ONLINE', btnW, btnH, {colors.cyan, colors.white}, function()
      resetTicketFlow()
      stopAudio(); state.page = 'online'
    end)
  end
  waitButtons()
end

local function showCardHome()
  while state.page == 'card_home' do
    drawHeader('IC Card Service', 'Open card or redeem online order')
    Buttons = {}
    local btnW, btnH = 16, 3
    local cx = math.floor((w - btnW) / 2) + 1
    local y = math.floor(h / 2) - 3
    addButton(cx, y, 'OPEN CARD', btnW, btnH, {colors.orange, colors.white}, function()
      resetCardFlow('open')
      state.page = 'card_name'
    end)
    addButton(cx, y + 4, 'ONLINE REDEEM', btnW, btnH, {colors.cyan, colors.white}, function()
      resetCardFlow('redeem')
      state.cardPaymentMode = 'online'
      state.page = 'card_online'
    end)
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'home' end)
    waitButtons()
  end
end

local function showCardNameInput()
  local name = firstString(state.holderName)
  local msg, msgCol = '', colors.red
  local maxNameLen = 24
  local keyboardMode = 'alpha'
  local alphaRows = {
    {'Q','W','E','R','T','Y','U','I','O','P'},
    {'A','S','D','F','G','H','J','K','L'},
    {'Z','X','C','V','B','N','M'}
  }
  local symbolRows = {
    {'-','_','.','\'',','},
    {'(',')','&','@','/'},
    {'+'}
  }

  local function isAllowedHolderChar(ch)
    if type(ch) ~= 'string' or #ch ~= 1 then return false end
    if ch:match('[A-Za-z ]') then return true end
    return ch == '.' or ch == ',' or ch == '\'' or ch == '(' or ch == ')' or ch == '&' or ch == '@' or ch == '/' or ch == '_' or ch == '-' or ch == '+'
  end

  local function appendHolderChar(ch)
    if not isAllowedHolderChar(ch) or #name >= maxNameLen then return end
    if ch:match('[A-Za-z]') then ch = ch:upper() end
    if ch == ' ' and (#name == 0 or name:sub(-1) == ' ') then return end
    name = name .. ch
  end

  while state.page == 'card_name' do
    local placeholder = name
    if #placeholder == 0 then
      placeholder = 'ENTER NAME'
    elseif #placeholder > (w - 8) then
      placeholder = '...' .. placeholder:sub(-(w - 11))
    end
    local rows = (keyboardMode == 'symbol') and symbolRows or alphaRows
    drawHeader('Enter Holder Name', (keyboardMode == 'symbol') and 'Symbol keyboard' or 'Letters / symbols / space')
    centerText(4, '[' .. placeholder .. ']', colors.yellow)
    if msg and #msg > 0 then centerText(5, msg, msgCol) end
    Buttons = {}
    local keyW, keyH = (w < 44 and 2 or 3), 2
    local sY = 7
    for rIdx, row in ipairs(rows) do
      local y = sY + (rIdx-1) * (keyH + 1)
      local rowW = math.max(0, #row * (keyW + 1) - 1)
      local x = math.max(1, math.floor((w - rowW) / 2) + 1)
      for _, ch in ipairs(row) do
        addButton(x, y, ch, keyW, keyH, {colors.black, colors.white}, function()
          appendHolderChar(ch)
        end)
        x = x + keyW + 1
      end
    end
    addButton(math.max(1, math.floor((w - 12) / 2) + 1), sY + 10, 'SPACE', 12, 2, {colors.gray, colors.white}, function()
      appendHolderChar(' ')
    end)
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'card_home' end)
    addButton(12, h-3, (keyboardMode == 'symbol') and 'ABC' or 'SYM', 8, 3, {colors.black, colors.orange}, function()
      keyboardMode = (keyboardMode == 'symbol') and 'alpha' or 'symbol'
    end)
    addButton(w-31, h-3, 'BKSP', 8, 3, {colors.black, colors.red}, function() name = name:sub(1, -2) end)
    addButton(w-21, h-3, 'CLEAR', 8, 3, {colors.black, colors.red}, function() name = '' end)
    addButton(w-11, h-3, 'NEXT->', 10, 3, {colors.black, colors.green}, function()
      local clean = firstString(name)
      if #clean < 2 then
        msg, msgCol = 'Name too short', colors.red
        return
      end
      state.holderName = clean
      state.page = 'card_topup'
    end)
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == 'mouse_click' or ev == 'monitor_touch' then
      for _, b in ipairs(Buttons) do
        if inRect(b, p2, p3) then clickSound(); if b.onClick then b.onClick() end; break end
      end
    elseif ev == 'char' then
      appendHolderChar(tostring(p1 or ''))
    elseif ev == 'key' and p1 == keys.backspace then
      name = name:sub(1, -2)
    elseif ev == 'key' and (p1 == keys.enter or p1 == keys.numPadEnter) then
      local clean = firstString(name)
      if #clean >= 2 then
        state.holderName = clean
        state.page = 'card_topup'
      else
        msg, msgCol = 'Name too short', colors.red
      end
    end
  end
end

local function showCardTopup()
  while state.page == 'card_topup' do
    local cardCfg = getCardConfig()
    state.cardTopup = math.max(cardCfg.min_topup, tonumber(state.cardTopup) or cardCfg.min_topup)
    state.cardBalance = state.cardTopup
    state.cardDeposit = 0
    state.cardOrderValue = state.cardTopup
    state.cost = state.cardOrderValue
    drawHeader('First Recharge', 'Holder: ' .. firstString(state.holderName, 'CARD USER'))
    Buttons = {}
    centerText(6, 'First Recharge: ' .. tostring(state.cardTopup), colors.yellow)
    centerText(8, 'Need Pay: ' .. tostring(state.cardOrderValue), colors.red)
    local colCount = 2
    local btnW, btnH = 12, 3
    local gap = 2
    local startX = math.max(2, math.floor((w - (colCount * btnW + (colCount - 1) * gap)) / 2) + 1)
    local startY = 11
    for idx, amount in ipairs(cardCfg.quick_amounts) do
      local row = math.floor((idx - 1) / colCount)
      local col = (idx - 1) % colCount
      addButton(startX + col * (btnW + gap), startY + row * (btnH + 1), tostring(amount), btnW, btnH,
        {state.cardTopup == amount and colors.green or colors.gray, colors.white},
        function() state.cardTopup = amount end)
    end
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'card_name' end)
    addButton(math.max(12, math.floor(w / 2) - 7), h-3, '-10', 6, 3, {colors.black, colors.red}, function()
      state.cardTopup = math.max(cardCfg.min_topup, state.cardTopup - 10)
    end)
    addButton(math.max(20, math.floor(w / 2)), h-3, '+10', 6, 3, {colors.black, colors.green}, function()
      state.cardTopup = math.min(999, state.cardTopup + 10)
    end)
    addButton(w-11, h-3, 'NEXT->', 10, 3, {colors.black, colors.green}, function()
      state.cardPaymentMode = 'local'
      state.page = 'order'
    end)
    waitButtons()
  end
end

local function showCardOnlineRedeem()
  local code = ''
  local msg, msgCol = '', colors.red
  local rows = {
    {'1','2','3','4','5','6','7','8','9','0'},
    {'Q','W','E','R','T','Y','U','I','O','P'},
    {'A','S','D','F','G','H','J','K','L'},
    {'Z','X','C','V','B','N','M'}
  }

  local function submitCode()
    if #code ~= 5 then
      msg, msgCol = 'Need 5 chars', colors.red
      return
    end
    local res = fetchJSON(API_BASE .. '/public/ic-cards/orders/' .. code)
    if not (res and res.ok) then
      res = fetchJSON(API_BASE .. '/public/orders/' .. code)
    end
    if not (res and res.ok) then
      msg, msgCol = 'Voucher Invalid', colors.red
      return
    end
    local d = res.data or res
    local cardStatus = tostring(d.status or ''):lower()
    if d.consumed or (cardStatus ~= '' and cardStatus ~= 'pending_pickup' and isCardOrderLike(d)) then
      msg, msgCol = 'Already Used!', colors.red
      return
    end
    local cardOrder = buildCardOrderState(d, code)
    if not cardOrder then
      msg, msgCol = 'Not a card order', colors.red
      return
    end
    resetCardFlow('redeem')
    state.holderName = cardOrder.holderName
    state.cardDeposit = cardOrder.deposit
    state.cardTopup = cardOrder.topup
    state.cardBalance = cardOrder.balance
    state.cardOrderValue = cardOrder.orderValue
    state.cost = state.cardOrderValue
    state.paid = 0
    state.voucher_code = code
    state.cardPaymentMode = 'local'
    state.card_server_data = cardOrder.raw
    state.page = 'order'
  end

  while state.page == 'card_online' do
    local placeholder = code .. string.rep('_', math.max(0, 5-#code))
    drawHeader('Redeem IC Card', 'Type 5 chars then OK')
    centerText(4, '[' .. placeholder .. ']', colors.yellow)
    if msg and #msg > 0 then centerText(5, msg, msgCol) end
    Buttons = {}
    local keyW, keyH = (w < 44 and 2 or 3), 2
    local kbW = 10 * (keyW + 1)
    local sX, sY = math.max(1, math.floor((w - kbW) / 2) + 1), 7
    for rIdx, row in ipairs(rows) do
      local y, x = sY + (rIdx-1) * (keyH + 1), sX + (rIdx-1)
      for _, ch in ipairs(row) do
        addButton(x, y, ch, keyW, keyH, {colors.black, colors.white}, function()
          if #code < 5 then code = code .. ch end
        end)
        x = x + keyW + 1
      end
    end
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'card_home' end)
    addButton(w-31, h-3, 'BKSP', 8, 3, {colors.black, colors.red}, function() code = code:sub(1, -2) end)
    addButton(w-21, h-3, 'CLEAR', 8, 3, {colors.black, colors.red}, function() code = '' end)
    addButton(w-11, h-3, 'OK', 10, 3, {colors.black, colors.green}, submitCode)
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == 'mouse_click' or ev == 'monitor_touch' then
      for _, b in ipairs(Buttons) do
        if inRect(b, p2, p3) then clickSound(); if b.onClick then b.onClick() end; break end
      end
    elseif ev == 'char' and #code < 5 then
      code = code .. tostring(p1 or ''):upper()
    elseif ev == 'key' and p1 == keys.backspace then
      code = code:sub(1, -2)
    elseif ev == 'key' and (p1 == keys.enter or p1 == keys.numPadEnter) then
      submitCode()
    end
  end
end

local ui_selected_code, ui_scroll_offset = nil, 0
local function renderLinesSelection(title, selectedCode, backPage, nextPage, infoLine)
  drawHeader(title, infoLine or 'Tap a station to select')
  Buttons = {}; local startY, endY = 5, h - 4
  local sbX = w - 3
  
  local function computeHeight()
    local y = startY
    for _, l in ipairs(CFG.lines) do
      y = y + 2; local x = 2
      local stopsList = {}
      if type(l) == 'table' then
        if type(l.stations) == 'table' then stopsList = l.stations
        elseif type(l.stops) == 'table' then stopsList = l.stops
        elseif type(l['站点列表']) == 'table' then stopsList = l['站点列表']
        elseif type(l['站点']) == 'table' then stopsList = l['站点']
        end
      end
      for _, sc in ipairs(stopsList) do
        local c = normalizeCode(sc)
        local st = stationByCode[c]
        local label = (st and (st.en_name or st.name)) or c
        local bw = #label + 1
        if x + bw > sbX - 1 then y = y + 4; x = 2 end
        x = x + bw + 2
      end
      y = y + 4
    end
    return y - startY
  end

  local ch = computeHeight(); local maxO = math.max(0, ch - (endY-startY+1))
  ui_scroll_offset = math.min(ui_scroll_offset, maxO)
  
  addButton(sbX, startY-1, '^', 3, 1, {colors.black, colors.white}, function() ui_scroll_offset = math.max(0, ui_scroll_offset-4) end)
  addButton(sbX, endY+1, 'v', 3, 1, {colors.black, colors.white}, function() ui_scroll_offset = math.min(maxO, ui_scroll_offset+4) end)
  
  local y = startY
  for _, l in ipairs(CFG.lines) do
    local py = y - ui_scroll_offset
    if py >= startY and py <= endY then
      termDev.setTextColor(nearestCCColor(type(l) == 'table' and l.color or nil)); termDev.setCursorPos(2, py); termDev.write(tostring(type(l) == 'table' and l.en_name or ''))
    end
    y = y + 1
    local by = y - ui_scroll_offset
    if by >= startY and by <= endY then
      local cc = nearestCCColor(type(l) == 'table' and l.color or nil); termDev.setBackgroundColor(cc); termDev.setCursorPos(2, by); termDev.write(string.rep(' ', sbX-4))
      termDev.setBackgroundColor(colors.black)
    end
    y = y + 1; local x = 2
    local stopsList = {}
    if type(l) == 'table' then
      if type(l.stations) == 'table' then stopsList = l.stations
      elseif type(l.stops) == 'table' then stopsList = l.stops
      elseif type(l['站点列表']) == 'table' then stopsList = l['站点列表']
      elseif type(l['站点']) == 'table' then stopsList = l['站点']
      end
    end
    for _, sc in ipairs(stopsList) do
      local c = normalizeCode(sc)
      local st = stationByCode[c]
      local label = (st and (st.en_name or st.name)) or c
      local bw = #label + 1
      if x + bw > sbX - 1 then y = y + 4; x = 2 end
      local ry = y - ui_scroll_offset
      if ry >= startY and ry+2 <= endY then
        addButton(x, ry, label, bw, 3, {selectedCode == c and colors.green or colors.gray, colors.white}, function() ui_selected_code = c end)
      end
      x = x + bw + 2
    end
    y = y + 4
  end
  
  addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = backPage end)
  if selectedCode then
    addButton(w-15, h-3, 'NEXT->', 10, 3, {colors.black, colors.green}, function() state.page = nextPage end)
  end
  return selectedCode
end

local function showDeparture()
  local sel; stopAudio(); playAudioFile('Audio/xzqd.wav')
  while state.page == 'departure' do
    sel = renderLinesSelection('Select Departure', sel, 'home', 'terminal', 'From: ' .. stationDisplay(sel)); addCancelButton()
    waitButtons()
    if ui_selected_code then
      sel = ui_selected_code
      ui_selected_code = nil
      sel = normalizeCode(sel)
      state.departure = sel
    end
    if ui_cancel_request then
      renderConfirmCancel()
      waitButtons()
      if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false end
      ui_cancel_request = false
    end
  end
end

local function showTerminal()
  local sel; stopAudio(); playAudioFile('Audio/xzzd.wav')
  local invalidUntil = 0
  while state.page == 'terminal' do
    local info = 'From: ' .. stationDisplay(state.departure) .. '  To: ' .. stationDisplay(sel)
    if invalidUntil > os.epoch('utc') then
      info = info .. '  (Invalid: same station)'
    end
    sel = renderLinesSelection('Select Terminal', sel, 'departure', 'type', info); addCancelButton()
    waitButtons()
    if ui_selected_code then
      sel = ui_selected_code
      ui_selected_code = nil
      sel = normalizeCode(sel)
      if sameLogicalStation(sel, state.departure) then
        invalidUntil = os.epoch('utc') + 1500
        sel = nil
        state.terminal = nil
        showAlert('Same station')
      else
        state.terminal = sel
      end
    end
    if ui_cancel_request then
      renderConfirmCancel()
      waitButtons()
      if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false end
      ui_cancel_request = false
    end
  end
end

local function showType()
  stopAudio(); playAudioFile('Audio/xzlc.wav')
  while state.page == 'type' do
    drawHeader('Select Train Type', 'From: ' .. stationDisplay(state.departure) .. '  To: ' .. stationDisplay(state.terminal))
    Buttons = {}
    local btnW, btnH = 12, 3
    local y = math.floor(h/2) - 2
    local x1, x2 = math.floor(w/2) - btnW - 2, math.floor(w/2) + 3
    if x1 < 2 or (x2 + btnW) > w-1 then
      local cx = math.floor((w - btnW) / 2) + 1
      addButton(cx, y-2, 'Local', btnW, btnH, {state.trainType == 'Local' and colors.green or colors.gray, colors.white}, function() state.trainType = 'Local' end)
      addButton(cx, y+2, 'Express', btnW, btnH, {state.trainType == 'Express' and colors.blue or colors.gray, colors.white}, function() state.trainType = 'Express' end)
    else
      addButton(x1, y, 'Local', btnW, btnH, {state.trainType == 'Local' and colors.green or colors.gray, colors.white}, function() state.trainType = 'Local' end)
      addButton(x2, y, 'Express', btnW, btnH, {state.trainType == 'Express' and colors.blue or colors.gray, colors.white}, function() state.trainType = 'Express' end)
    end
    addCancelButton()
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'terminal' end)
    if state.trainType then addButton(w-9, h-3, 'NEXT->', 8, 3, {colors.black, colors.green}, function() state.page = 'trips' end) end
    waitButtons()
    if ui_cancel_request then
      renderConfirmCancel()
      waitButtons()
      if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false end
      ui_cancel_request = false
    end
  end
end

local function showTrips()
  while state.page == 'trips' do
    drawHeader('Select Trips', 'From: ' .. stationDisplay(state.departure) .. '  To: ' .. stationDisplay(state.terminal))
    Buttons = {}
    local y = math.floor(h/2) - 2
    local midW = 14
    local xMid = math.floor((w - midW) / 2) + 1
    addButton(xMid, y, tostring(state.trips) .. (state.trips == 1 and ' TRIP' or ' TRIPS'), midW, 3, {colors.gray, colors.white}, function() end)
    addButton(xMid-5, y, '+', 4, 3, {colors.black, colors.green}, function() state.trips = math.min(99, (tonumber(state.trips) or 1) + 1) end)
    addButton(xMid+midW+1, y, '-', 4, 3, {colors.black, colors.red}, function() state.trips = math.max(1, (tonumber(state.trips) or 1) - 1) end)
    addCancelButton()
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'type' end)
    addButton(w-9, h-3, 'NEXT->', 8, 3, {colors.black, colors.green}, function() state.page = 'order' end)
    waitButtons()
    if ui_cancel_request then
      renderConfirmCancel()
      waitButtons()
      if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false end
      ui_cancel_request = false
    end
  end
end

local function computeCost(src, dst, trainType)
  src = normalizeCode(src)
  dst = normalizeCode(dst)
  if sameLogicalStation(src, dst) then return 0 end
  local adj = trainType == 'Express' and adjacency_express or adjacency_regular

  local heapN, heapD = {}, {}
  local function heapPush(n, d)
    local i = #heapN + 1
    heapN[i] = n; heapD[i] = d
    while i > 1 do
      local p = math.floor(i / 2)
      if heapD[p] <= heapD[i] then break end
      heapN[p], heapN[i] = heapN[i], heapN[p]
      heapD[p], heapD[i] = heapD[i], heapD[p]
      i = p
    end
  end
  local function heapPop()
    if #heapN == 0 then return nil end
    local n, d = heapN[1], heapD[1]
    local ln, ld = heapN[#heapN], heapD[#heapD]
    heapN[#heapN], heapD[#heapD] = nil, nil
    if #heapN > 0 then
      heapN[1], heapD[1] = ln, ld
      local i = 1
      while true do
        local l = i * 2
        local r = l + 1
        if l > #heapN then break end
        local m = l
        if r <= #heapN and heapD[r] < heapD[l] then m = r end
        if heapD[i] <= heapD[m] then break end
        heapN[i], heapN[m] = heapN[m], heapN[i]
        heapD[i], heapD[m] = heapD[m], heapD[i]
        i = m
      end
    end
    return n, d
  end

  local dist = { [src] = 0 }
  heapPush(src, 0)
  while true do
    local u, du = heapPop()
    if not u then break end
    if du == dist[u] then
      if u == dst then return math.floor(du + 0.5) end
      for v, w in pairs(adj[u] or {}) do
        local nd = du + (tonumber(w) or 0)
        if nd < (dist[v] or math.huge) then
          dist[v] = nd
          heapPush(v, nd)
        end
      end
    end
  end
  return nil
end

local function paymentHintText()
  if detectedPaymentSide and #tostring(detectedPaymentSide) > 0 then
    return 'Payment side: ' .. tostring(detectedPaymentSide):upper()
  end
  return 'Insert payment on any side'
end

local function snapshotPaymentInputs()
  local states = {}
  if not redstone or type(redstone.getInput) ~= 'function' then return states end
  for _, side in ipairs(REDSTONE_SIDES) do
    states[side] = redstone.getInput(side) and true or false
  end
  return states
end

local function detectPaymentPulse(prevStates)
  local nowStates = snapshotPaymentInputs()
  for _, side in ipairs(REDSTONE_SIDES) do
    if nowStates[side] and not prevStates[side] then
      detectedPaymentSide = side
      return true, side, nowStates
    end
  end
  return false, nil, nowStates
end

local function drawOrder()
  if state.productMode == 'card' then
    local cardSub = (state.cardMode == 'redeem') and 'Redeem IC card order' or 'Open new stored-value card'
    drawHeader('Confirm Card', cardSub)
  else
    local depLine = (state.trainType == 'single') and state.departure or stationDisplay(state.departure)
    local terLine = (state.trainType == 'single') and 'Any 1 Segment' or stationDisplay(state.terminal)
    drawHeader('Confirm Order', 'From: ' .. depLine .. '  To: ' .. terLine)
  end
  local y = 5
  local function L(label, value, col)
    termDev.setTextColor(colors.white)
    termDev.setCursorPos(2, y); termDev.write(label .. ': ')
    termDev.setTextColor(col or colors.lightBlue)
    termDev.write(tostring(value or 'N/A'))
    y = y + 2
  end

  local cost = tonumber(state.cost)
  local paid = tonumber(state.paid) or 0
  local total = tonumber(state.cost)
  if state.productMode == 'card' then
    L('Action', (state.cardMode == 'redeem') and 'Online Redeem' or 'Open Card', colors.lightBlue)
    L('Name', firstString(state.holderName, 'CARD USER'), colors.yellow)
    L('Top-up', tonumber(state.cardTopup) or 0, colors.lightBlue)
    L('Balance', tonumber(state.cardBalance) or 0, colors.cyan)
    L('Order', tonumber(state.cardOrderValue) or 0, colors.red)
    if state.cardPaymentMode == 'online' then
      L('Payment', 'Paid Online', colors.green)
    else
      L('Pay Now', total or 0, colors.red)
    end
    if state.voucher_code then
      L('Voucher', tostring(state.voucher_code), colors.cyan)
    end
  else
    L('Type', state.trainType or 'N/A', colors.lightBlue)
    local rides = tonumber(state.trips) or 1
    if cost == nil then
      if state.trainType ~= 'single' then
        local unit = computeCost(state.departure or '', state.terminal or '', state.trainType or 'Local')
        cost = unit and (unit * rides) or nil
      end
    end
    L('Trips', rides)
    L('Cost', cost, colors.red)
    if state.voucher_code then
      L('Voucher', tostring(state.voucher_code), colors.cyan)
    end
    local discount = (CFG.promotion and CFG.promotion.discount) or 1
    if discount > 0 and discount < 1 then
      local promoName = (CFG.promotion and CFG.promotion.name) or 'Promo'
      local perc = math.floor(discount * 100)
      local promoText = ('[PROMO] %s • Discount: %d%%'):format(promoName, perc)
      drawRainbowLabelRow(y, promoText, colors.black)
      y = y + 2
    end
  end
  if total == nil then total = cost or 0 end
  local remain = math.max(0, total - paid)
  local statusY = math.max(1, h - 6)
  local barW = math.max(10, w - 10)
  local ratio = (total <= 0) and 1 or math.min(1, paid / math.max(1, total))
  local fill = math.floor(barW * ratio)
  local barX = math.max(1, math.floor((w - barW) / 2) + 1)
  termDev.setCursorPos(barX, statusY)
  termDev.setTextColor(colors.green)
  termDev.write(string.rep('=', fill))
  termDev.setTextColor(colors.gray)
  termDev.write(string.rep('-', math.max(0, barW - fill)))
  statusY = statusY + 1
  if remain <= 0 then
    termDev.setTextColor(colors.green)
    centerText(statusY, ('Paid: %d / %d  (OK)'):format(paid, total), colors.green)
  else
    termDev.setTextColor(colors.red)
    centerText(statusY, ('Paid: %d / %d  Remaining: %d'):format(paid, total, remain), colors.red)
  end
  if total <= 0 then
    centerText(statusY + 1, 'Ready to confirm', colors.lightGray)
  else
    centerText(statusY + 1, paymentHintText(), colors.lightGray)
  end
end

local function showOrderAndAudio()
  state.paid = 0; state.doneAudioPlayed = false; state.order_datetime = os.date('%Y/%m/%d %H:%M:%S')
  
  -- Recalculate cost immediately to avoid stale data
  if state.productMode == 'card' then
    state.cardDeposit = 0
    state.cardOrderValue = tonumber(state.cardOrderValue) or (tonumber(state.cardTopup) or 0)
    state.cardBalance = tonumber(state.cardBalance) or tonumber(state.cardTopup) or 0
    if state.cardPaymentMode ~= 'online' then
      state.cost = state.cardOrderValue
    elseif state.cost == nil then
      state.cost = 0
    end
  elseif not state.voucher_code then
    if state.trainType == 'single' then
      -- Cost is already set in showSingleSegmentSelection
      if state.cost == nil then state.cost = 0 end
    else
      local unit = computeCost(state.departure or '', state.terminal or '', state.trainType or 'Local')
      if not unit then
        showAlert('No route')
        state.page = 'terminal'
        return
      end
      state.cost = unit * (state.trips or 1)
    end
  else
    if state.cost == nil then state.cost = 0 end
  end

  if state.productMode ~= 'card' and state.trainType ~= 'single' and sameLogicalStation(state.departure, state.terminal) then
    showAlert('Same station')
    state.page = state.voucher_code and 'online' or 'terminal'
    return
  end
  
  local confirmed = false
  local processing = false
  local statusMsg, statusCol = '', colors.red
  local render = nil
  
  local function confirmAction()
    if processing or confirmed then return end
    if state.productMode ~= 'card' and sameLogicalStation(state.departure, state.terminal) then
      showAlert('Same station')
      if render then render() end
      return
    end
    processing = true
    statusMsg, statusCol = 'Processing...', colors.yellow
    if render then render() end
    if state.productMode == 'card' then
      local payload = {
        holder_name = firstString(state.holderName, 'CARD USER'),
        deposit = tonumber(state.cardDeposit) or 0,
        topup = tonumber(state.cardTopup) or 0,
        balance = tonumber(state.cardBalance) or 0,
        order_value = tonumber(state.cardOrderValue) or 0,
        voucher_code = state.voucher_code,
        station_code = state.stationCode or CURRENT_STATION_CODE,
        device = currentDeviceId(),
        card_mode = state.cardMode or 'open',
        payment_mode = state.cardPaymentMode or 'local',
        amount_paid = tonumber(state.paid) or 0
      }
      local reuseBlankCardId = firstString(state.pendingBlankCardId)
      if #reuseBlankCardId > 0 then
        payload.card_id = reuseBlankCardId
      else
        payload.card_id = generateCardId()
      end
      local okIssueBlank, blankCardId, issueMethod = false, '', 'reuse_pending'
      if #reuseBlankCardId == 0 then
        okIssueBlank, blankCardId, issueMethod = issueBlankICCard(payload.holder_name, payload.balance)
      else
        okIssueBlank, blankCardId = true, reuseBlankCardId
      end
      if okIssueBlank and #blankCardId > 0 then
        payload.card_id = blankCardId
        state.pendingBlankCardId = blankCardId
        local okReq, code, parsed, err = submitCardOpen(payload)
        if okReq then
          local respData = (type(parsed) == 'table' and (parsed.data or parsed.card or parsed)) or {}
          local finalCard = buildFinalCardData(payload, respData)
          local okWrite, writtenCard, writeMethod = writeICCard(finalCard, { writeOnly = true })
          if okWrite then
            state.card_id = firstString(writtenCard.card_id, finalCard.card_id)
            state.cardBalance = tonumber(writtenCard.balance) or finalCard.balance
            state.card_server_data = respData
            state.pendingBlankCardId = nil
            confirmed = true
            statusMsg, statusCol = 'Card ready', colors.green
            if not state.doneAudioPlayed then playConfirmTicketMelody(); state.doneAudioPlayed = true end
          else
            statusMsg, statusCol = 'Write failed: ' .. tostring(writeMethod), colors.red
          end
        else
          local errorMsg = 'Card API Err'
          if code == 409 then
            errorMsg = 'Already Used!'
          elseif err and #tostring(err) > 0 then
            errorMsg = tostring(err)
          elseif code and type(code) == 'number' then
            errorMsg = 'HTTP ' .. tostring(code)
          end
          statusMsg, statusCol = 'Issued, retry sync: ' .. errorMsg, colors.red
        end
      elseif okIssueBlank then
        statusMsg, statusCol = 'Card issued without ID', colors.red
      else
        local okReq, code, parsed, err = submitCardOpen(payload)
        if okReq then
          local respData = (type(parsed) == 'table' and (parsed.data or parsed.card or parsed)) or {}
          local finalCard = buildFinalCardData(payload, respData)
          local okWrite, writtenCard, writeMethod = writeICCard(finalCard)
          if okWrite then
            state.card_id = firstString(writtenCard.card_id, finalCard.card_id)
            state.cardBalance = tonumber(writtenCard.balance) or finalCard.balance
            state.card_server_data = respData
            state.pendingBlankCardId = nil
            confirmed = true
            statusMsg, statusCol = 'Card ready', colors.green
            if not state.doneAudioPlayed then playConfirmTicketMelody(); state.doneAudioPlayed = true end
          else
            statusMsg, statusCol = 'Write failed: ' .. tostring(writeMethod), colors.red
          end
        else
          local errorMsg = 'Card API Err'
          if code == 409 then
            errorMsg = 'Already Used!'
          elseif err and #tostring(err) > 0 then
            errorMsg = tostring(err)
          elseif code and type(code) == 'number' then
            errorMsg = 'HTTP ' .. tostring(code)
          end
          statusMsg, statusCol = errorMsg, colors.red
        end
      end
    elseif state.voucher_code then
      local url = API_BASE .. '/public/orders/' .. state.voucher_code .. '/consume'
      local okReq, code = postJSONResult(url, {})
      if okReq and code and code >= 200 and code < 300 then
        confirmed = true
        if not state.doneAudioPlayed then playConfirmTicketMelody(); state.doneAudioPlayed = true end
      else
        local errorMsg = 'NetErr'
        if code == 409 then
          errorMsg = 'Already Used!'
        elseif code and type(code) == 'number' then
          errorMsg = 'HTTP ' .. tostring(code)
        end
        statusMsg, statusCol = errorMsg, colors.red
      end
    else 
      confirmed = true 
      local totalCost = tonumber(state.cost) or 0
      if totalCost <= 0 and not state.doneAudioPlayed then playConfirmTicketMelody(); state.doneAudioPlayed = true end
    end
    processing = false
    if render and not confirmed then render() end
  end

  render = function()
    Buttons = {}
    drawOrder()
    addCancelButton()
    local same = (state.productMode ~= 'card') and sameLogicalStation(state.departure, state.terminal)
    local costV = tonumber(state.cost)
    local paidV = tonumber(state.paid) or 0
    local canConfirm = (not same) and ((costV or 0) <= 0 or (costV ~= nil and paidV >= costV))
    local msgY = h - 2
    if statusMsg and #statusMsg > 0 then centerText(msgY, statusMsg, statusCol) end
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function()
      stopAudio()
      if state.productMode == 'card' then
        state.page = state.voucher_code and 'card_online' or 'card_topup'
      else
        state.page = state.voucher_code and 'online' or 'trips'
      end
    end)
    if canConfirm then
      if processing then
        addButton(w-12, h-3, 'WAIT', 10, 3, {colors.gray, colors.white}, function() end)
      else
        addButton(w-12, h-3, 'CONFIRM', 10, 3, {colors.green, colors.black}, confirmAction)
      end
    end
  end
  render()
  
  -- If cost is zero, auto-confirm immediately after a brief delay for audio
  if ((state.productMode == 'card') or (not sameLogicalStation(state.departure, state.terminal))) and ((tonumber(state.cost) or 0) <= 0) then
    sleep(0.5)
    confirmAction()
  end
  
  local prevInputs = snapshotPaymentInputs()
  while state.page == 'order' do
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == 'redstone' then
      local pulsed, _, nextInputs = detectPaymentPulse(prevInputs)
      if pulsed then
        playNote('hat', 20, 1, 0.01)
        state.paid = (state.paid or 0) + 1; render()
        if state.paid >= (state.cost or 0) then
          if not state.doneAudioPlayed then
            playConfirmTicketMelody(); state.doneAudioPlayed = true
          end
          -- Auto-confirm when paid enough
          sleep(0.5) -- Wait for UI/Audio slightly
          confirmAction()
        end
      end
      prevInputs = nextInputs
    elseif ev == 'mouse_click' or ev == 'monitor_touch' then
      -- For mouse_click: p1=button, p2=x, p3=y
      -- For monitor_touch: p1=side, p2=x, p3=y
      for _, b in ipairs(Buttons) do 
        if inRect(b, p2, p3) then 
          clickSound()
          if b.onClick then b.onClick() end; 
          render() 
          break
        end 
      end
      if ui_cancel_request then
        renderConfirmCancel()
        waitButtons()
        if ui_cancel_confirmed then stopAudio(); state.page = 'home'; ui_cancel_confirmed = false; break end
        ui_cancel_request = false
        render()
      end
    elseif ev == 'config_updated' then
      render()
    end
    if confirmed then state.page = 'done'; break end
  end
end

local function showPrePrintCheck()
  while state.page == 'preprint' do
    state.page = 'done'
  end
end

local function generateTicketId()
  return generateNumericCode('TK', 8)
end


local function showDone()
  if state.productMode == 'card' then
    drawHeader('Card Ready', 'Please take your IC card')
    local y = 5
    local function line(label, value, col)
      termDev.setTextColor(colors.white)
      termDev.setCursorPos(2, y); termDev.write(label .. ': ')
      termDev.setTextColor(col or colors.lightBlue)
      termDev.write(tostring(value or ''))
      y = y + 2
    end
    line('Name', firstString(state.holderName, 'CARD USER'), colors.yellow)
    line('Deposit', tonumber(state.cardDeposit) or 0, colors.lightBlue)
    line('Top-up', tonumber(state.cardTopup) or 0, colors.lightBlue)
    line('Balance', tonumber(state.cardBalance) or 0, colors.green)
    line('Card ID', firstString(state.card_id, 'PENDING'), colors.cyan)
    if state.voucher_code then line('Voucher', state.voucher_code, colors.cyan) end
    saveCardIssueSnapshot({
      card_id = state.card_id,
      holder_name = state.holderName,
      deposit = state.cardDeposit,
      topup = state.cardTopup,
      balance = state.cardBalance,
      voucher_code = state.voucher_code,
      station_code = state.stationCode or CURRENT_STATION_CODE,
      ts = (os.epoch and os.epoch('utc')) or (os.time() * 1000)
    })
    for i = 5, 1, -1 do
      centerText(h-4, 'Returning to Home: ' .. i .. 's', colors.red)
      sleep(1)
    end
    resetTicketFlow()
    state.page = 'home'
    return
  end
  local rides = tonumber(state.trips) or 1
  local orderDatetime = state.order_datetime or os.date('%Y/%m/%d %H:%M:%S')
  local rawCost = tonumber(state.cost)
  if (not state.voucher_code) and rawCost == nil then
    local unit = computeCost(state.departure or '', state.terminal or '', state.trainType or 'Local')
    rawCost = unit and (unit * rides) or 0
    state.cost = rawCost
  end
  local cost = rawCost or 0
  local issueSource = 'local'
  local startCode = normalizeCode(state.departure)
  local terminalCode = normalizeCode(state.terminal)
  local startObj = stationByCode[startCode]
  local terminalObj = stationByCode[terminalCode]
  local fromNameEn = (startObj and (startObj.en_name or startObj.name)) or state.departure
  local toNameEn = (terminalObj and (terminalObj.en_name or terminalObj.name)) or state.terminal
  local fromNameCnU = (startObj and unicodeEscape(startObj.name)) or ''
  local toNameCnU = (terminalObj and unicodeEscape(terminalObj.name)) or ''
  local startStationArg = tostring(startCode or '')
  local terminalStationArg = tostring(terminalCode or '')
  local fromNameEnArg = tostring(fromNameEn or '???')
  local toNameEnArg = tostring(toNameEn or '???')
  local fromNameCnUArg = tostring(fromNameCnU or '')
  local toNameCnUArg = tostring(toNameCnU or '')
  local issueArgsType = 'local'
  if state.trainType == 'Express' then issueArgsType = 'limited_express'
  elseif state.trainType == 'single' then issueArgsType = 'single' end
  local issueArgs = {
    start_name_en = fromNameEnArg,
    terminal_name_en = toNameEnArg,
    type = issueArgsType,
    rides = rides,
    cost = cost,
    start_station = startStationArg,
    terminal_station = terminalStationArg,
    fromNameCnU = fromNameCnUArg,
    toNameCnU = toNameCnUArg,
  }
  _G.TICKET_MACHINE_LAST_TICKET = {
    start_station_id = startCode,
    terminal_station_id = terminalCode,
    start_station = startCode,
    terminal_station = terminalCode,
    start_name = startObj and unicodeEscape(startObj.name) or nil,
    terminal_name = terminalObj and unicodeEscape(terminalObj.name) or nil,
    start_name_en = fromNameEn,
    terminal_name_en = toNameEn,
    ts = (os.epoch and os.epoch('utc')) or (os.time() * 1000)
  }

  local apiType = 'local'
  if state.trainType == 'Express' then apiType = 'limited_express'
  elseif state.trainType == 'single' then apiType = 'single' end
  local localGeneratedTicketId = generateTicketId()
  local okIssueTicket, issuedTicketId, issueMethod = issueTicketFromPeripheral(
    fromNameEnArg,
    toNameEnArg,
    apiType,
    rides,
    cost,
    startStationArg,
    terminalStationArg,
    fromNameCnUArg,
    toNameCnUArg,
    localGeneratedTicketId
  )
  if okIssueTicket then
    state.ticket_id = issuedTicketId
    issueSource = 'ticket_vending_machine'
  else
    local issueError = tostring(issueMethod or 'ticket_issue_failed')
    print('Ticket issue failed: ' .. issueError)
    _G.TICKET_MACHINE_LAST_TICKET.ticket_issue_error = issueError
    showAlert('Ticket issue failed')
    resetTicketFlow()
    state.page = 'home'
    return
  end

  pcall(function()
    ensureDir('logs/last_ticket_issue.json')
    issueArgs.ticket_id = state.ticket_id
    issueArgs.issue_source = issueSource
    issueArgs.ts = (os.epoch and os.epoch('utc')) or (os.time() * 1000)
    local okSer, s = pcall(textutils.serializeJSON, issueArgs)
    if okSer and type(s) == 'string' then
      local f = fs.open('logs/last_ticket_issue.json', 'w')
      if f then f.write(s); f.close() end
    end
  end)

  local typeStr = (state.trainType == 'Express') and 'Limited Express' or 'Ordinary'
  local ticketData = {
    start_station = startCode,
    terminal_station = terminalCode,
    end_station = terminalCode,
    type = typeStr,
    entered = false,
    exited = false,
    id = state.ticket_id,
    ticket_id = state.ticket_id,
    trips_total = rides,
    trips_remaining = rides,
    cost = cost,
    order_datetime = orderDatetime,
    timestamp = (os.epoch and os.epoch('utc')) or (os.time() * 1000)
  }

  local ticketDataMod = {}
  for k, v in pairs(ticketData) do ticketDataMod[k] = v end
  ticketDataMod.start_station_id = startCode
  ticketDataMod.terminal_station_id = terminalCode
  ticketDataMod.start_station = startCode
  ticketDataMod.terminal_station = terminalCode
  ticketDataMod.start_name = startObj and unicodeEscape(startObj.name) or nil
  ticketDataMod.terminal_name = terminalObj and unicodeEscape(terminalObj.name) or nil
  ticketDataMod.start_name_en = fromNameEn
  ticketDataMod.terminal_name_en = toNameEn
  ticketDataMod.issue_source = issueSource
  ticketDataMod.ticket_id = state.ticket_id
  ticketDataMod.train_type = state.trainType or 'Local'
  ticketDataMod.station_code = state.stationCode or CURRENT_STATION_CODE
  ticketDataMod.device = currentDeviceId()
  _G.TICKET_MACHINE_LAST_TICKET = ticketDataMod

  if MOD_DEBUG then
    local t = _G.TICKET_MACHINE_LAST_TICKET
    print("DEBUG start_name_en=" .. tostring(t.start_name_en))
    print("DEBUG terminal_name_en=" .. tostring(t.terminal_name_en))
    print("DEBUG cost=" .. tostring(t.cost))
  end

  local okUpload, uploadRes = pcall(enqueueTicketUpload, ticketDataMod)
  if not okUpload then
    print('enqueueTicketUpload error: ' .. tostring(uploadRes))
  end
  drawHeader('Purchase Complete', 'Please take your ticket')
  local y = 5
  local function line(label, value, col)
    termDev.setTextColor(colors.white)
    termDev.setCursorPos(2, y); termDev.write(label .. ': ')
    termDev.setTextColor(col or colors.lightBlue)
    termDev.write(tostring(value or ''))
    y = y + 2
  end
  line('From', fromNameEn, colors.yellow)
  line('To', toNameEn, colors.yellow)
  line('Type', typeStr, colors.lightBlue)
  line('Trips', rides, colors.lightBlue)
  line('Cost', cost, colors.red)
  line('ID', state.ticket_id or '', colors.cyan)

  
  
  for i = 5, 1, -1 do
    centerText(h-4, 'Returning to Home: ' .. i .. 's', colors.red)
    sleep(1)
  end
  resetTicketFlow()
  state.page = 'home'
end

local function showOnlineVoucher()
  local code = ''
  local msg, msgCol = '', colors.red
  local rows = {
    {'1','2','3','4','5','6','7','8','9','0'},
    {'Q','W','E','R','T','Y','U','I','O','P'},
    {'A','S','D','F','G','H','J','K','L'},
    {'Z','X','C','V','B','N','M'}
  }
  
  local function submitCode()
    if #code == 5 then
      local res = fetchJSON(API_BASE .. '/public/orders/' .. code)
      if res and res.ok then
        local d = res.data or res
        
        -- Check if already consumed
        if d.consumed then
          msg, msgCol = 'Already Used!', colors.red
          return
        end
        
        state.departure = d.start or d.from or d.departure
        state.terminal = d.terminal or d.to or d.destination
        state.trainType = normalizeTrainTypeLabel(d.train_type or d.type or 'Local')
        state.trips = tonumber(d.trips or d.count) or 1
        state.cost = tonumber(d.price) or tonumber(d.cost) or 0
        state.voucher_code = code
        msg = ''
        state.page = 'order'
      else
        msg, msgCol = 'Voucher Invalid', colors.red
      end
    end
  end

  while state.page == 'online' do
    local placeholder = code .. string.rep('_', math.max(0, 5-#code))
    drawHeader('Enter Voucher', 'Type 5 chars then OK')
    centerText(4, '[' .. placeholder .. ']', colors.yellow)
    if msg and #msg > 0 then centerText(5, msg, msgCol) end
    Buttons = {}
    
    local keyW, keyH = (w < 44 and 2 or 3), 2
    local kbW = 10 * (keyW + 1)
    local sX, sY = math.max(1, math.floor((w - kbW) / 2) + 1), 7
    
    for rIdx, row in ipairs(rows) do
      local y, x = sY + (rIdx-1) * (keyH + 1), sX + (rIdx-1)
      for _, ch in ipairs(row) do
        addButton(x, y, ch, keyW, keyH, {colors.black, colors.white}, function() if #code < 5 then code = code .. ch end end)
        x = x + keyW + 1
      end
    end
    
    local actY = h - 6
    local bw, cw, ow = 10, 8, 8
    local gap = 2
    local total = bw + gap + cw + gap + ow
    local ax = math.floor((w - total) / 2) + 1
    if ax < 1 then
      bw, cw, ow = 6, 6, 6
      total = bw + 1 + cw + 1 + ow
      ax = math.floor((w - total) / 2) + 1
      addButton(math.max(1, ax), actY, 'BKSP', bw, 3, {colors.black, colors.red}, function() code = code:sub(1, -2) end)
      addButton(math.max(1, ax) + bw + 1, actY, 'CLEAR', cw, 3, {colors.black, colors.red}, function() code = '' end)
      addButton(math.max(1, ax) + bw + 1 + cw + 1, actY, 'OK', ow, 3, {colors.black, colors.green}, submitCode)
    else
      addButton(ax, actY, 'Backspace', bw, 3, {colors.black, colors.red}, function() code = code:sub(1, -2) end)
      addButton(ax + bw + gap, actY, 'Clear', cw, 3, {colors.black, colors.red}, function() code = '' end)
      addButton(ax + bw + gap + cw + gap, actY, 'OK', ow, 3, {colors.black, colors.green}, submitCode)
    end
    addButton(2, h-3, '<-back', 8, 3, {colors.black, colors.red}, function() state.page = 'home' end)
    
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == 'mouse_click' or ev == 'monitor_touch' then
      for _, b in ipairs(Buttons) do if inRect(b, p2, p3) then clickSound(); if b.onClick then b.onClick() end; break end end
    elseif ev == 'char' and #code < 5 then code = code .. p1:upper()
    elseif ev == 'key' and p1 == keys.backspace then code = code:sub(1, -2)
    elseif ev == 'key' and (p1 == keys.enter or p1 == keys.numPadEnter) then submitCode()
    elseif ev == 'config_updated' then
    end
  end
end

-- ###########################
-- Main entry point
-- ###########################
local function mainPageLoop()
  while true do
    if state.page == 'home' then showHome()
    elseif state.page == 'single_segment' then showSingleSegmentSelection()
    elseif state.page == 'card_home' then showCardHome()
    elseif state.page == 'card_name' then showCardNameInput()
    elseif state.page == 'card_topup' then showCardTopup()
    elseif state.page == 'card_online' then showCardOnlineRedeem()
    elseif state.page == 'departure' then showDeparture()
    elseif state.page == 'terminal' then showTerminal()
    elseif state.page == 'type' then showType()
    elseif state.page == 'trips' then showTrips()
    elseif state.page == 'order' then showOrderAndAudio()
    elseif state.page == 'preprint' then showPrePrintCheck()
    elseif state.page == 'done' then showDone()
    elseif state.page == 'online' then showOnlineVoucher()
    else state.page = 'home'; sleep(0.5) end
  end
end

parallel.waitForAny(mainPageLoop, backgroundSyncTask, backgroundTicketUploadTask, backgroundPeripheralTask)
