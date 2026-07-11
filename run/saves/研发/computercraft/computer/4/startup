local DEFAULT_SERVER_BASE = "http://ticket.fse-media.group"
local DEFAULT_SERVER_PATH = "/api/tickets/check"
local GATE_OPEN_SECONDS = 2
local VERSION = "v1.5.13.2"
local VERSION_CHECK_INTERVAL = 5

local CONFIG_PATH = "gate_config.json"

local function readFile(path)
  if not fs.exists(path) then return nil end
  local f = fs.open(path, "r")
  if not f then return nil end
  local c = f.readAll()
  f.close()
  return c
end

local function writeFile(path, content)
  local f = fs.open(path, "w")
  if not f then return false end
  f.write(content)
  f.close()
  return true
end

local function trim(s)
  return (tostring(s or ""):gsub("^%s+", ""):gsub("%s+$", ""))
end

local function normalizeVersionTag(v)
  local s = trim(v)
  if #s == 0 then return "" end
  if s:sub(1, 1):lower() ~= "v" then
    s = "v" .. s
  end
  return s:lower()
end

local function splitCsv(s)
  local out = {}
  s = trim(s)
  if #s == 0 then return out end
  for part in s:gmatch("[^,/%s]+") do
    local v = trim(part)
    if #v > 0 then table.insert(out, v) end
  end
  return out
end

local pack = table.pack or function(...)
  return { n = select("#", ...), ... }
end

local function loadConfig()
  local def = { mode = "entry", station_codes = {} }
  local raw = readFile(CONFIG_PATH)
  if not raw or #raw == 0 then return def end
  local ok, data = pcall(textutils.unserializeJSON, raw)
  if not ok or type(data) ~= "table" then return def end
  if type(data.mode) == "string" then def.mode = data.mode end
  if type(data.station_codes) == "table" then def.station_codes = data.station_codes end
  if type(data.side_modes) == "table" then def.side_modes = data.side_modes end
  if type(data.server_url) == "string" then def.server_url = data.server_url end
  if type(data.card_server_url) == "string" then def.card_server_url = data.card_server_url end
  if type(data.station_code) == "string" then def.station_code = data.station_code end
  if type(data.side_station_codes) == "table" then def.side_station_codes = data.side_station_codes end
  return def
end

local function stationSetFromList(list)
  local set = {}
  if type(list) ~= "table" then return set end
  for _, v in ipairs(list) do
    local c = trim(v)
    if #c > 0 then
      local parts = splitCsv(c)
      if #parts == 0 then
        set[c] = true
      else
        for _, p in ipairs(parts) do set[p] = true end
      end
    end
  end
  return set
end

local monitor = peripheral.find("monitor")
local speaker = peripheral.find("speaker")
local inspection = peripheral.find("ticket_inspection_machine")

local serverConnected = nil
local versionMismatch = nil
local expectedGateVersion = nil
local failedUpdateVersion = nil
local pendingGateUpdate = false
local remoteForceUpdate = false
local isUpdating = false
local gateBusyUntilTs = 0

local function nowMs()
  return (os.epoch and os.epoch("utc")) or (os.time() * 1000)
end

local function markGateBusy(seconds)
  local secondsNum = tonumber(seconds) or 0
  local untilTs = nowMs() + math.max(1000, math.floor(secondsNum * 1000))
  if untilTs > gateBusyUntilTs then
    gateBusyUntilTs = untilTs
  end
end

local function isGateIdleForUpdate()
  return nowMs() >= gateBusyUntilTs
end

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

local function runSilentGateUpdate()
  if not fs.exists("update_gate.lua") then
    return false, "update_gate.lua not found"
  end
  local baseEnv = (getfenv and getfenv()) or _ENV or _G
  local env = setmetatable({ AUTO_UPDATE_SILENT = true }, { __index = baseEnv })
  local fn, err = loadfile("update_gate.lua", env)
  if not fn then return false, err end
  local ok, res = pcall(fn, "--silent")
  if not ok then return false, tostring(res) end
  return true, true
end

local function setServerConnected(ok)
  if serverConnected == ok then return end
  serverConnected = ok
  serverLastChangeTs = os.epoch("utc")
end

local termDev = term
if monitor then
  pcall(monitor.setTextScale, 0.5)
  termDev = monitor
end

local function clear()
  termDev.setBackgroundColor(colors.black)
  termDev.setTextColor(colors.white)
  termDev.clear()
  termDev.setCursorPos(1, 1)
end

local function centerText(y, text, color)
  local w = termDev.getSize()
  termDev.setTextColor(color or colors.white)
  local x = math.max(1, math.floor((w - #text) / 2) + 1)
  termDev.setCursorPos(x, y)
  termDev.write(text)
end

local function drawServerStatusIndicator(w)
  if w < 2 then return end
  local col = colors.yellow
  if serverConnected == true then col = colors.lime
  elseif serverConnected == false then col = colors.red end
  termDev.setBackgroundColor(colors.black)
  termDev.setTextColor(col)
  termDev.setCursorPos(w - 1, 1)
  termDev.write("S")
  termDev.setCursorPos(w, 1)
  termDev.write("*")
  termDev.setTextColor(colors.white)
end

local lastUpdateError = nil

local function drawVersionIndicator(w)
  local s = tostring(VERSION or "")
  if #s == 0 then return end
  if w < #s then return end
  local markerColor = colors.yellow
  local markerText = "*   "
  if isUpdating then
    markerColor = colors.yellow
    markerText = "^ing"
  elseif versionMismatch == true then
    markerColor = colors.red
    markerText = "*   "
  elseif versionMismatch == false then
    markerColor = colors.lime
    markerText = "    "
  end
  termDev.setBackgroundColor(colors.black)
  termDev.setTextColor(colors.gray)
  termDev.setCursorPos(1, 1)
  termDev.write(s)
  termDev.setTextColor(markerColor)
  termDev.write(markerText)
  
  if lastUpdateError then
    termDev.setCursorPos(1, 2)
    termDev.setTextColor(colors.red)
    termDev.write(string.sub(tostring(lastUpdateError), 1, w))
  end
  
  termDev.setTextColor(colors.white)
end

local function draw(statusLine1, statusLine2, statusColor)
  clear()
  local w, h = termDev.getSize()
  centerText(2, "GATE", colors.cyan)
  drawVersionIndicator(w)
  drawServerStatusIndicator(w)
  if statusLine1 and #statusLine1 > 0 then
    centerText(math.max(2, math.floor(h / 2)), statusLine1, statusColor or colors.white)
  end
  if statusLine2 and #statusLine2 > 0 then
    centerText(math.min(h, math.max(3, math.floor(h / 2) + 1)), statusLine2, statusColor or colors.white)
  end
  termDev.setCursorPos(1, h)
  termDev.setTextColor(colors.gray)
  termDev.write(string.rep(" ", w))
end

local function pulseLeftRedstone(seconds)
  seconds = tonumber(seconds) or 1
  if not redstone or type(redstone.setOutput) ~= "function" then return end
  pcall(redstone.setOutput, "left", true)
  os.sleep(seconds)
  pcall(redstone.setOutput, "left", false)
end

pcall(function()
  if redstone and type(redstone.setOutput) == "function" then
    redstone.setOutput("left", false)
  end
end)

local function readApiEndpointFile(path)
  local s = trim(readFile(path) or "")
  if #s == 0 then return nil end
  return s
end

local function resolveServerURL(cfg)
  if type(cfg.server_url) == "string" and #trim(cfg.server_url) > 0 then
    local u = trim(cfg.server_url)
    u = u:gsub("/api/tickets/status%s*$", "/api/tickets/check")
    return u
  end

  local base = readApiEndpointFile("API_ENDPOINT_GATE.txt") or readApiEndpointFile("API_ENDPOINT.txt")
  if base and base:match("/api$") then
    base = base:sub(1, -5)
  end
  if base and #base > 0 then
    return base .. DEFAULT_SERVER_PATH
  end
  return DEFAULT_SERVER_BASE .. DEFAULT_SERVER_PATH
end

local function guessBaseFromStatusURL(url)
  url = trim(url or "")
  if #url == 0 then return DEFAULT_SERVER_BASE end
  local b = url:gsub("/api/tickets/check.*$", "")
  b = b:gsub("/api/.*$", "")
  b = trim(b)
  if #b == 0 then return DEFAULT_SERVER_BASE end
  return b
end

local function httpRequest(method, url, body, headers)
  if not http then
    setServerConnected(false)
    return false, "HTTP API disabled"
  end
  headers = headers or {}
  local okReq, err = pcall(function()
    http.request({
      url = url,
      method = method,
      headers = headers,
      body = body,
    })
  end)
  if not okReq then
    setServerConnected(false)
    return false, tostring(err)
  end

  while true do
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == "http_success" and p1 == url then
      local res = p2
      if type(res) == "table" and type(res.readAll) == "function" then
        local data = res.readAll()
        res.close()
        setServerConnected(true)
        return true, data
      end
      setServerConnected(false)
      return false, "invalid http response"
    end
    if ev == "http_failure" and p1 == url then
      local err = p2
      local res = p3
      if type(p2) == "table" and type(p2.readAll) == "function" then
        res = p2
        err = p3
      end
      if type(res) == "table" and type(res.readAll) == "function" then
        local data = res.readAll()
        res.close()
        setServerConnected(false)
        return false, data
      end
      setServerConnected(false)
      return false, tostring(err or "http_failure")
    end
    os.queueEvent(ev, p1, p2, p3)
    os.sleep(0)
  end
end

local function postCheck(url, payload)
  local okBody, body = pcall(textutils.serializeJSON, payload)
  if not okBody then return false end
  local ok, data = httpRequest("POST", url, body, { ["Content-Type"] = "application/json" })
  if not ok then return false, data end
  local okJ, parsed = pcall(textutils.unserializeJSON, data or "")
  if not okJ then return false, data end
  return true, parsed
end

local function getJSON(url)
  local ok, data = httpRequest("GET", url)
  if not ok then return false, data end
  local okJ, parsed = pcall(textutils.unserializeJSON, data or "")
  if not okJ then return false, data end
  return true, parsed
end

local function resolveCardServerURL(cfg, ticketCheckURL)
  if type(cfg.card_server_url) == "string" and #trim(cfg.card_server_url) > 0 then
    return trim(cfg.card_server_url)
  end
  local base = guessBaseFromStatusURL(ticketCheckURL)
  return base:gsub("/+$", "") .. "/api/cards/check"
end

local function resolveCardSyncBaseURL(cfg, ticketCheckURL, cardCheckURL)
  if type(cfg.card_sync_url) == "string" and #trim(cfg.card_sync_url) > 0 then
    local raw = trim(cfg.card_sync_url)
    return raw:gsub("/+$", "")
  end
  local base = guessBaseFromStatusURL(cardCheckURL or ticketCheckURL)
  return base:gsub("/+$", "") .. "/api/ic-cards"
end

local function resolveFareQueryURL(ticketCheckURL)
  local base = guessBaseFromStatusURL(ticketCheckURL)
  return base:gsub("/+$", "") .. "/api/public/fares/query"
end

local function urlEncodeComponent(value)
  local s = tostring(value or "")
  return (s:gsub("([^%w%-_%.~])", function(c)
    return string.format("%%%02X", string.byte(c))
  end))
end

local function toMoney(v)
  local n = tonumber(v)
  if n == nil then return nil end
  return math.floor(n * 100 + 0.5) / 100
end

local stationNameToCode = {}

local function normKey(s)
  return trim(s):lower()
end

local function refreshStationNameMap(serverBase)
  serverBase = trim(serverBase or "")
  if #serverBase == 0 then return false end
  local url = serverBase:gsub("/+$", "") .. "/api/stations"
  local ok, data = httpRequest("GET", url)
  if not ok then return false end
  local okJ, parsed = pcall(textutils.unserializeJSON, data or "")
  if not okJ then return false end
  if type(parsed) == "table" and type(parsed.stations) == "table" then
    parsed = parsed.stations
  end
  if type(parsed) ~= "table" then return false end

  stationNameToCode = {}
  for _, st in ipairs(parsed) do
    if type(st) == "table" then
      local code = trim(st.code)
      if #code > 0 then
        local en = trim(st.en_name or st.en)
        if #en > 0 then stationNameToCode[normKey(en)] = code end
        local cn = trim(st.name)
        if #cn > 0 then stationNameToCode[normKey(cn)] = code end
      end
    end
  end
  return true
end

local function refreshRemoteLuaVersion(serverBase)
  serverBase = trim(serverBase or "")
  if #serverBase == 0 then return false end
  local url = serverBase:gsub("/+$", "") .. "/api/public/config"
  local ok, parsed = getJSON(url)
  if not ok or type(parsed) ~= "table" then return false end
  local remote = normalizeVersionTag(type(parsed.lua_versions) == "table" and parsed.lua_versions.gate or nil)
  remoteForceUpdate = (parsed.force_update == true)
  if #remote == 0 then
    expectedGateVersion = nil
    versionMismatch = nil
    return true
  end
  expectedGateVersion = remote
  versionMismatch = (remote ~= normalizeVersionTag(VERSION))
  return true
end


local function inferStationCodeFromName(name)
  local key = normKey(name or "")
  if #key == 0 then return "" end
  return stationNameToCode[key] or ""
end

local function playDfpwm(path)
  if not speaker then return end
  if not fs.exists(path) then return end
  local okD, dfpwm = pcall(require, "cc.audio.dfpwm")
  if not okD or not dfpwm then return end
  local h = fs.open(path, "rb")
  if not h then return end
  local decoder = dfpwm.make_decoder()
  while true do
    local chunk = h.read(16 * 1024)
    if not chunk then break end
    local buf = decoder(chunk)
    while not speaker.playAudio(buf) do
      os.pullEvent("speaker_audio_empty")
    end
  end
  h.close()
end

local function normalizeTicketId(v)
  v = tostring(v or "")
  v = v:gsub("^%s+", ""):gsub("%s+$", "")
  v = v:gsub("%s+", "")
  if #v == 0 then return nil end
  local prefix, num = v:match("^([A-Za-z][A-Za-z])%-?([0-9]+)$")
  if prefix and num then
    prefix = prefix:upper()
    if #num < 8 then
      num = string.rep("0", 8 - #num) .. num
    elseif #num > 8 then
      num = num:sub(-8)
    end
    return prefix .. "-" .. num
  end
  return v:lower()
end

local function normalizeIcCardId(v)
  local s = tostring(v or "")
  s = s:gsub("%s+", ""):upper()
  if #s == 0 then return "" end
  local num = s:match("^IC%-?([0-9]+)$")
  if num then
    if #num < 6 then
      num = string.rep("0", 6 - #num) .. num
    elseif #num > 6 then
      num = num:sub(-6)
    end
    return "IC-" .. num
  end
  return s
end

local function collectScanTables(scan, includeTicket)
  local out = {}
  local seen = {}
  local function add(v)
    if type(v) ~= "table" or seen[v] then return end
    seen[v] = true
    table.insert(out, v)
  end
  if type(scan) ~= "table" then return out end
  add(scan)
  add(scan.data)
  add(scan.payload)
  add(scan.card)
  add(scan.ic_card)
  add(scan.wallet)
  add(scan.card_data)
  add(scan.media_data)
  if includeTicket then
    add(scan.ticket)
    add(scan.ticket_data)
  end
  return out
end

local function firstNonEmptyFromTables(tables, keys)
  if type(tables) ~= "table" or type(keys) ~= "table" then return "" end
  for _, t in ipairs(tables) do
    for _, key in ipairs(keys) do
      local v = t[key]
      if v ~= nil then
        local s = trim(v)
        if #s > 0 then return s end
      end
    end
  end
  return ""
end

local function firstNumberFromTables(tables, keys)
  if type(tables) ~= "table" or type(keys) ~= "table" then return nil end
  for _, t in ipairs(tables) do
    for _, key in ipairs(keys) do
      local n = tonumber(t[key])
      if n ~= nil then return n end
    end
  end
  return nil
end

local function isTruthy(v)
  if v == true then return true end
  if type(v) == "number" then return v ~= 0 end
  if type(v) == "string" then
    local s = v:lower()
    return s == "true" or s == "1" or s == "yes"
  end
  return false
end

local function firstTruthyFromTables(tables, keys)
  if type(tables) ~= "table" or type(keys) ~= "table" then return false end
  for _, t in ipairs(tables) do
    for _, key in ipairs(keys) do
      if t[key] ~= nil and isTruthy(t[key]) then
        return true
      end
    end
  end
  return false
end

local function getTicketId(scan)
  local tables = collectScanTables(scan, true)
  if #tables == 0 then return nil end
  local raw = firstNonEmptyFromTables(tables, {
    "ticketId", "ticket_id", "id", "ticketNo", "ticket_no", "code"
  })
  if #raw == 0 then return nil end
  return normalizeTicketId(raw)
end



local function getCardId(scan)
  local tables = collectScanTables(scan, false)
  if #tables == 0 then return "" end
  local raw = firstNonEmptyFromTables(tables, {
    "card_id",
    "cardId",
    "ic_card_id",
    "icCardId",
    "wallet_id",
    "walletId",
    "card_uid",
    "cardUid",
    "uid",
    "uuid",
    "serial",
    "serial_number",
    "serialNumber",
    "nfc_uid",
    "nfcUid",
    "rfid_uid",
    "rfidUid"
  })
  if #raw == 0 then return "" end
  return normalizeIcCardId(raw)
end

local function getCardBalance(scan)
  local tables = collectScanTables(scan, false)
  return firstNumberFromTables(tables, {
    "balance",
    "stored_value",
    "storedValue",
    "wallet_balance",
    "walletBalance",
    "remaining_balance",
    "remainingBalance",
    "value",
    "amount"
  })
end

local function isICCardScan(scan)
  local tables = collectScanTables(scan, false)
  if #tables == 0 then return false end
  if #getCardId(scan) > 0 then return true end
  if getCardBalance(scan) ~= nil then return true end
  if type(scan.card) == "table" or type(scan.ic_card) == "table" or type(scan.wallet) == "table" then
    return true
  end
  local media = firstNonEmptyFromTables(tables, {
    "media",
    "media_type",
    "mediaType",
    "product_type",
    "productType",
    "ticket_type",
    "ticketType",
    "kind",
    "type",
    "category"
  }):lower()
  if media:find("card", 1, true) or media:find("wallet", 1, true) then return true end
  if media:find("ic", 1, true) or media:find("nfc", 1, true) or media:find("rfid", 1, true) then return true end
  return false
end

local function getStartStation(scan)
  local tables = collectScanTables(scan, true)
  local id = firstNonEmptyFromTables(tables, {
    "entry",
    "start_station",
    "startStation",
    "start",
    "start_station_id",
    "start_station_code",
    "from_station",
    "from",
    "startStationId",
    "start_stationId",
    "entry_station",
    "entryStation"
  })
  if #id > 0 then return id end
  return inferStationCodeFromName(firstNonEmptyFromTables(tables, {
    "start_name_en", "startNameEn", "start_name", "fromNameCnU", "fromNameCn", "entry_name", "entryName"
  }))
end

local function getTerminalStation(scan)
  local tables = collectScanTables(scan, true)
  local id = firstNonEmptyFromTables(tables, {
    "exit",
    "terminal_station",
    "terminalStation",
    "terminal",
    "end_station",
    "endStation",
    "terminal_station_id",
    "terminal_station_code",
    "to_station",
    "to",
    "endStationId",
    "end_stationId",
    "exit_station",
    "exitStation"
  })
  if #id > 0 then return id end
  return inferStationCodeFromName(firstNonEmptyFromTables(tables, {
    "terminal_name_en", "terminalNameEn", "terminal_name", "toNameCnU", "toNameCn", "exit_name", "exitName"
  }))
end


local function saveLastScan(scan)
  if type(scan) ~= "table" then return end
  local t = {}
  for k, v in pairs(scan) do t[k] = v end
  local startStation = getStartStation(t)
  if #startStation > 0 and (t.start_station == nil or trim(t.start_station) == "") then
    t.start_station = startStation
  end
  local terminalStation = getTerminalStation(t)
  if #terminalStation > 0 and (t.terminal_station == nil or trim(t.terminal_station) == "") then
    t.terminal_station = terminalStation
  end

  local ok, s = pcall(textutils.serializeJSON, t)
  if not ok or type(s) ~= "string" then
    ok, s = pcall(textutils.serialize, t)
  end
  if ok and type(s) == "string" then
    writeFile("last_scan.json", s)
  end
end

local cfg = loadConfig()
local stationSet = stationSetFromList(cfg.station_codes)
local serverURL = resolveServerURL(cfg)
local cardServerURL = resolveCardServerURL(cfg, serverURL)
local cardSyncBaseURL = resolveCardSyncBaseURL(cfg, serverURL, cardServerURL)
local fareQueryURL = resolveFareQueryURL(serverURL)
local mode = (trim(cfg.mode):lower() == "exit") and "exit" or "entry"

local modeBySide = nil
if type(cfg.side_modes) == "table" then
  local tmp = {}
  for side, m in pairs(cfg.side_modes) do
    if type(side) == "string" then
      local s = trim(side):lower()
      if #s > 0 then
        tmp[s] = (trim(m):lower() == "exit") and "exit" or "entry"
      end
    end
  end
  if next(tmp) ~= nil then modeBySide = tmp end
end

local sideStationCodeBySide = nil
if type(cfg.side_station_codes) == "table" then
  local tmp = {}
  for side, code in pairs(cfg.side_station_codes) do
    if type(side) == "string" then
      local s = trim(side):lower()
      local c = trim(code)
      if #s > 0 and #c > 0 then
        tmp[s] = c
      end
    end
  end
  if next(tmp) ~= nil then sideStationCodeBySide = tmp end
end

pcall(function()
  refreshStationNameMap(guessBaseFromStatusURL(serverURL))
end)

pcall(function()
  refreshRemoteLuaVersion(guessBaseFromStatusURL(serverURL))
end)

if not inspection then
  if modeBySide == nil then
    draw("Missing peripheral:", "ticket_inspection_machine", colors.red)
    error("ticket_inspection_machine not found")
  end
end

if next(stationSet) == nil then
  draw("No station codes set.", "Run installer first.", colors.red)
  error("No station codes configured")
end

local stationCodesPayload = {}
for k, _ in pairs(stationSet) do table.insert(stationCodesPayload, k) end
table.sort(stationCodesPayload)

local function trimSide(s)
  s = trim(s or ""):lower()
  if #s == 0 then return nil end
  return s
end

local function defaultStationCode()
  local direct = trim(cfg.station_code or "")
  if #direct > 0 then return direct end
  return trim(stationCodesPayload[1] or "")
end

local function stationCodeForSide(side)
  side = trimSide(side)
  if side and type(sideStationCodeBySide) == "table" then
    local v = trim(sideStationCodeBySide[side] or "")
    if #v > 0 then return v end
  end
  return defaultStationCode()
end

local function isInspectionPeripheral(p)
  return type(p) == "table" and (
    type(p.getLastScanned) == "function"
    or type(p.updateICCard) == "function"
    or type(p.updateTicket) == "function"
    or type(p.destroyTicket) == "function"
  )
end

local function resolveInspection(side)
  side = trimSide(side)
  if side and peripheral and type(peripheral.wrap) == "function" then
    local okW, p = pcall(peripheral.wrap, side)
    if okW and isInspectionPeripheral(p) then
      return p
    end
    return nil
  end
  if isInspectionPeripheral(inspection) then return inspection end
  return nil
end

local function validateBidirectional()
  if not modeBySide then return true end
  for side, _ in pairs(modeBySide) do
    if not resolveInspection(side) then
      draw("Missing peripheral:", "ticket_inspection_machine@" .. tostring(side), colors.red)
      error("ticket_inspection_machine not found on side: " .. tostring(side))
    end
  end
  return true
end

validateBidirectional()

local function inferSideFromScan(scan)
  if type(scan) ~= "table" then return nil end
  return trimSide(
    scan.side
      or scan.source_side
      or scan.reader_side
      or scan.peripheral_side
      or scan.peripheralSide
      or scan.device_side
      or scan.peripheral
      or scan.source
      or scan.reader
      or scan.name
  )
end

local function isSideName(s)
  s = trimSide(s)
  if not s then return false end
  return s == "front" or s == "back" or s == "left" or s == "right" or s == "top" or s == "bottom"
end

local function parseTicketScannedArgsPacked(ev)
  local side = nil
  local scan = nil
  if type(ev) ~= "table" then return nil, nil end
  local n = tonumber(ev.n) or #ev
  for i = 2, n do
    local v = ev[i]
    if not scan and type(v) == "table" then
      scan = v
    elseif not side and type(v) == "string" and isSideName(v) then
      side = trimSide(v)
    end
  end
  if scan and not side then side = inferSideFromScan(scan) end
  return scan, side
end

local function actionForSide(side)
  if not modeBySide then return mode end
  side = trimSide(side)
  if not side then return mode end
  return modeBySide[side] or mode
end

local function shortModeLabel(v)
  v = trim(v):lower()
  if v == "exit" then return "OUT" end
  return "IN"
end

local function readyLine1()
  if not modeBySide then
    return "READY " .. shortModeLabel(mode)
  end
  return "F " .. shortModeLabel(modeBySide.front or "") .. "  B " .. shortModeLabel(modeBySide.back or "")
end

local function readyLine2()
  if not modeBySide then
    return "ST " .. stationCodeForSide(nil)
  end
  local frontCode = stationCodeForSide("front")
  local backCode = stationCodeForSide("back")
  if frontCode == backCode then
    return "ST " .. frontCode
  end
  return "F " .. frontCode .. "  B " .. backCode
end

local function drawReadyScreen()
  draw(readyLine1(), readyLine2(), colors.lime)
end

drawReadyScreen()

local function collectInspectionDevices(side, modeBySideRef)
  local sideKnown = trimSide(side) ~= nil
  local inspectionDevs = {}
  local function addDev(dev)
    if not dev then return end
    table.insert(inspectionDevs, dev)
  end
  if sideKnown then
    addDev(resolveInspection(side))
  elseif modeBySideRef then
    for s, _ in pairs(modeBySideRef) do
      addDev(resolveInspection(s))
    end
  else
    addDev(resolveInspection(side))
  end
  return inspectionDevs, sideKnown
end

local function collectInspectionBindings(side, modeBySideRef, fallbackInspection)
  local out = {}
  local seen = {}
  local function addBinding(sideName, dev)
    if not dev or seen[dev] then return end
    seen[dev] = true
    table.insert(out, { side = trimSide(sideName), dev = dev })
  end

  side = trimSide(side)
  if side then
    addBinding(side, resolveInspection(side))
  elseif modeBySideRef then
    for s, _ in pairs(modeBySideRef) do
      addBinding(s, resolveInspection(s))
    end
  else
    addBinding(nil, fallbackInspection or resolveInspection(nil))
  end
  return out
end

local function updateDeviceField(dev, key, value)
  if type(dev) ~= "table" or type(dev.updateTicket) ~= "function" then return end
  pcall(dev.updateTicket, key, value)
end

local function callTicketStateMethod(dev, methodName)
  if type(dev) ~= "table" then return false end
  local fn = dev[methodName]
  if type(fn) ~= "function" then return false end
  local okCall, okRes = pcall(fn)
  if not okCall then return false end
  return okRes ~= false
end

local function applyTicketDeviceState(dev, action, rides)
  if type(dev) ~= "table" then return end
  if action == "entry" then
    if not callTicketStateMethod(dev, "markEntered") then
      updateDeviceField(dev, "entered", true)
      updateDeviceField(dev, "exited", false)
    end
  elseif action == "exit" then
    if not callTicketStateMethod(dev, "markExited") then
      updateDeviceField(dev, "exited", true)
      updateDeviceField(dev, "entered", false)
    end
  elseif action == "reset" then
    if not callTicketStateMethod(dev, "resetTicketState") then
      updateDeviceField(dev, "entered", false)
      updateDeviceField(dev, "exited", false)
    end
  end
  if rides ~= nil then updateDeviceField(dev, "rides", rides) end
end

local function updateICCardField(dev, key, value)
  if type(dev) ~= "table" or type(dev.updateICCard) ~= "function" then return false end
  local okCall, okRes, detail = pcall(dev.updateICCard, key, value)
  if not okCall then return false, tostring(okRes) end
  if okRes == false then return false, tostring(detail or "update_failed") end
  return true
end

local function updateICCardFields(dev, patch)
  local allOk = true
  local firstErr = nil
  if type(patch) ~= "table" then return false end
  for key, value in pairs(patch) do
    local okField, errField = updateICCardField(dev, key, value)
    if not okField then
      allOk = false
      if not firstErr then
        firstErr = tostring(key) .. ": " .. tostring(errField or "update_failed")
      end
    end
  end
  return allOk, firstErr
end

local function readLastScanned(dev)
  if type(dev) ~= "table" or type(dev.getLastScanned) ~= "function" then return nil end
  local ok, scan = pcall(dev.getLastScanned)
  if not ok or type(scan) ~= "table" then return nil end
  return scan
end

local function getCardEntry(scan)
  return firstNonEmptyFromTables(collectScanTables(scan, false), {
    "entry", "entry_station", "entryStation", "start_station", "startStation"
  })
end

local function getCardEntered(scan)
  if #getCardEntry(scan) > 0 then return true end
  return firstTruthyFromTables(collectScanTables(scan, false), {
    "entered", "is_entered", "in_station", "inside_station"
  })
end

local function getCardExited(scan)
  if #getCardEntry(scan) > 0 then return false end
  return firstTruthyFromTables(collectScanTables(scan, false), {
    "exited", "is_exited", "out_station", "outside_station"
  })
end

local function getCardOwnerName(scan)
  return firstNonEmptyFromTables(collectScanTables(scan, false), {
    "ownerName", "owner_name", "holder_name", "card_holder", "passenger"
  })
end

local function queryFare(fromStation, toStation)
  fromStation = trim(fromStation)
  toStation = trim(toStation)
  if #fromStation == 0 or #toStation == 0 then
    return nil, "missing_station"
  end
  local url = fareQueryURL
    .. "?from=" .. urlEncodeComponent(fromStation)
    .. "&to=" .. urlEncodeComponent(toStation)
  local ok, resp = getJSON(url)
  if not ok or type(resp) ~= "table" then
    return nil, "net_error"
  end
  local fare = tonumber(
    resp.discounted_regular_fare
    or resp.discounted_regular
    or resp.regular_fare
    or resp["优惠后常规票价"]
    or resp["常规票价"]
    or resp.regular
  )
  if fare == nil then
    return nil, tostring(resp.error or resp["错误"] or "fare_not_found")
  end
  return toMoney(fare), nil
end

local function denyCard(reason, detail)
  draw("DENIED", tostring(detail or reason or "deny"), colors.red)
  playDfpwm("error.dfpwm")
end

local function deductICCardBalance(dev, amount)
  if type(dev) ~= "table" or type(dev.deductICCard) ~= "function" then
    return false, "unsupported_method"
  end
  local okCall, okRes, detail = pcall(dev.deductICCard, amount)
  if not okCall then return false, tostring(okRes) end
  if okRes == true then return true, tonumber(detail) end
  return false, tostring(detail or "deduct_failed")
end

local function currentDeviceId()
  local label = os.getComputerLabel()
  if label and label ~= "" then
    return label
  end
  return "#" .. tostring(os.getComputerID())
end

local function syncICCardState(cardId, payload)
  local id = trim(cardId)
  if #id == 0 then return false, "missing_card_id" end
  local url = cardSyncBaseURL .. "/" .. urlEncodeComponent(id) .. "/sync"
  payload = payload or {}
  payload.card_id = id
  return postCheck(url, payload)
end

local function handleICCardScan(scan, side, scanDev)
  saveLastScan(scan)
  local inspectionDevs = scanDev and { scanDev } or select(1, collectInspectionDevices(side, modeBySide, inspection))
  local sideKnown = trimSide(side) ~= nil
  local action = actionForSide(side)
  if modeBySide and not sideKnown then
    if getCardEntered(scan) and not getCardExited(scan) then
      action = "exit"
    else
      action = "entry"
    end
  end

  local cardId = getCardId(scan)
  if #cardId == 0 then
    draw("Invalid card.", "Missing card_id.", colors.red)
    playDfpwm("error.dfpwm")
    return
  end

  local balance = toMoney(getCardBalance(scan) or 0) or 0
  local entryStation = trim(getCardEntry(scan))
  local exitStation = stationCodeForSide(side)
  local fare = 0
  local usedAction = action

  if #exitStation == 0 then
    denyCard("missing_station", "Missing gate station")
    return
  end

  if usedAction == "entry" then
    if getCardEntered(scan) and not getCardExited(scan) then
      denyCard("already_entered", "Already entered")
      return
    end
    local okWrite = true
    local writeErr = nil
    for _, dev in ipairs(inspectionDevs) do
      local okPatch, errPatch = updateICCardFields(dev, {
        entry = exitStation,
      })
      okWrite = okPatch and okWrite
      if not okPatch and not writeErr then writeErr = errPatch end
    end
    if not okWrite then
      draw("WRITE ERROR", tostring(writeErr or "Failed to update card"), colors.red)
      playDfpwm("error.dfpwm")
      return
    end
    entryStation = exitStation
  else
    if not getCardEntered(scan) then
      denyCard("not_entered", "Not entered")
      return
    end
    if getCardExited(scan) then
      denyCard("already_exited", "Already exited")
      return
    end
    if #entryStation == 0 then
      denyCard("missing_entry_station", "Missing entry station")
      return
    end
    local fareValue, fareErr = queryFare(entryStation, exitStation)
    if fareValue == nil then
      if fareErr == "net_error" then
        draw("NET ERROR", "Fare lookup failed", colors.red)
        playDfpwm("error.dfpwm")
      else
        denyCard("fare_not_found", fareErr)
      end
      return
    end
    fare = fareValue
    if balance < fare then
      denyCard("insufficient_balance", "Fare: " .. tostring(fare) .. " Bal: " .. tostring(balance))
      return
    end
    local okWrite = true
    local writeErr = nil
    for _, dev in ipairs(inspectionDevs) do
      local okDeduct, newBalanceOrErr = deductICCardBalance(dev, fare)
      if okDeduct then
        balance = toMoney(newBalanceOrErr or (balance - fare)) or 0
        local okClear, clearErr = updateICCardField(dev, "entry", nil)
        local okFare, fareErr = updateICCardField(dev, "last_fare", fare)
        if not okClear or not okFare then
          okWrite = false
          if not okClear and not writeErr then writeErr = "entry: " .. tostring(clearErr or "update_failed") end
          if not okFare and not writeErr then writeErr = "last_fare: " .. tostring(fareErr or "update_failed") end
        end
      else
        okWrite = false
        if tostring(newBalanceOrErr) == "insufficient" then
          denyCard("insufficient_balance", "-" .. tostring(fare) .. " Left: " .. tostring(balance))
        else
          draw("WRITE ERROR", tostring(newBalanceOrErr or "deduct_failed"), colors.red)
          playDfpwm("error.dfpwm")
        end
        break
      end
    end
    if not okWrite then
      if writeErr then
        draw("WRITE ERROR", tostring(writeErr), colors.red)
        playDfpwm("error.dfpwm")
      end
      return
    end
  end

  for _, dev in ipairs(inspectionDevs) do
    if usedAction == "entry" then
      updateDeviceField(dev, "entered", true)
      updateDeviceField(dev, "exited", false)
      if #entryStation > 0 then updateDeviceField(dev, "entry_station", entryStation) end
    else
      updateDeviceField(dev, "exited", true)
      updateDeviceField(dev, "entered", false)
      if #exitStation > 0 then updateDeviceField(dev, "exit_station", exitStation) end
      updateDeviceField(dev, "last_fare", fare)
    end
    if balance ~= nil then updateDeviceField(dev, "balance", balance) end
  end

  local syncTs = (os.epoch and os.epoch("utc")) or (os.time() * 1000)
  local okSync, syncResp = syncICCardState(cardId, {
    type = "check",
    action = usedAction,
    device = currentDeviceId(),
    ts = syncTs,
    station_code = exitStation,
    entry_station = entryStation,
    exit_station = usedAction == "exit" and exitStation or "",
    entered = (usedAction == "entry"),
    exited = (usedAction == "exit"),
    fare = fare,
    last_fare = fare,
    balance = balance,
    result = "pass"
  })
  if okSync and type(syncResp) == "table" and type(syncResp.card) == "table" then
    balance = toMoney(syncResp.card.balance or balance) or balance
  end

  local line2 = nil
  if usedAction == "exit" then
    line2 = "-" .. tostring(fare) .. "  Left: " .. tostring(balance or "?")
  else
    line2 = "Left: " .. tostring(balance or "?")
  end
  draw("PASS", line2, colors.lime)
  parallel.waitForAll(
    function() pulseLeftRedstone(GATE_OPEN_SECONDS) end,
    function() playDfpwm("pass.dfpwm") end
  )
end

local function handleScan(scan, side, scanDev)
  saveLastScan(scan)
  local inspectionDevs = scanDev and { scanDev } or select(1, collectInspectionDevices(side, modeBySide, inspection))
  local sideKnown = trimSide(side) ~= nil
  local action = actionForSide(side)
  if modeBySide and not sideKnown then
    if isTruthy(scan and scan.entered) and not isTruthy(scan and scan.exited) then
      action = "exit"
    else
      action = "entry"
    end
  end

  local ticketId = getTicketId(scan)
  if not ticketId then
    draw("Invalid ticket.", "Missing ticketId.", colors.red)
    playDfpwm("error.dfpwm")
    return
  end

  local hintTripsTotal = tonumber(scan.trips_total or scan.rides_total or scan.trips or scan.rides)
  local hintTripsRemaining = tonumber(scan.trips_remaining or scan.rides_remaining)

  local function doCheck(act)
    return postCheck(serverURL, {
      ticket_id = ticketId,
      action = act,
      station_codes = stationCodesPayload,
      station_code = stationCodeForSide(side),
      device = currentDeviceId(),
      ts = os.epoch("utc"),
      trips_total = hintTripsTotal,
      trips_remaining = hintTripsRemaining,
    })
  end

  local ok, resp = doCheck(action)
  local usedAction = action
  if ok and type(resp) == "table" and resp.result ~= "pass" and tostring(resp.reason) == "wrong_station" and modeBySide and not sideKnown then
    local alt = (action == "entry") and "exit" or "entry"
    local ok2, resp2 = doCheck(alt)
    if ok2 and type(resp2) == "table" then
      if resp2.result == "pass" then
        ok, resp = ok2, resp2
        usedAction = alt
      elseif tostring(resp2.reason) ~= "wrong_station" then
        ok, resp = ok2, resp2
        usedAction = alt
      end
    end
  end

  if not ok or type(resp) ~= "table" then
    draw("NET ERROR", "Server check failed.", colors.red)
    playDfpwm("error.dfpwm")
    return
  end

  if resp.result ~= "pass" then
    draw("DENIED", tostring(resp.reason or "deny"), colors.red)
    playDfpwm("error.dfpwm")
    return
  end

  pcall(function()
    if #inspectionDevs == 0 then return end
    local newRides = tonumber(resp.trips_remaining)
      or tonumber(scan.trips_remaining or scan.rides_remaining)
      or tonumber(scan.rides)
    for _, dev in ipairs(inspectionDevs) do
      applyTicketDeviceState(dev, usedAction, newRides)
    end
  end)

  local remaining = tonumber(resp.trips_remaining)
  if usedAction == "exit" and isTruthy(resp.destroy_ticket) and remaining ~= nil and remaining <= 0 then
    for _, dev in ipairs(inspectionDevs) do
      applyTicketDeviceState(dev, "reset", remaining)
      if type(dev) == "table" and type(dev.destroyTicket) == "function" then
        pcall(dev.destroyTicket)
      end
    end
  end

  local msg = (usedAction == "exit")
    and ("Rides left: " .. tostring(resp.trips_remaining or ""))
    or "Welcome."

  draw("PASS", msg, colors.lime)
  parallel.waitForAll(
    function() pulseLeftRedstone(GATE_OPEN_SECONDS) end,
    function() playDfpwm("pass.dfpwm") end
  )
end

local recentScans = {}

local function buildScanKey(scan, side, eventName)
  side = trimSide(side) or "-"
  if eventName == "ic_card_scanned" or isICCardScan(scan) then
    return table.concat({
      side,
      "ic",
      getCardId(scan),
      tostring(getCardBalance(scan) or ""),
      getCardEntry(scan),
      getCardOwnerName(scan),
    }, "|")
  end
  return table.concat({
    side,
    "ticket",
    tostring(getTicketId(scan) or ""),
    tostring(scan.timestamp or scan.order_datetime or ""),
    tostring(scan.rides or scan.trips or ""),
    tostring(scan.entered or ""),
    tostring(scan.exited or ""),
  }, "|")
end

local function shouldProcessScan(scan, side, eventName)
  local key = buildScanKey(scan, side, eventName)
  local now = os.epoch("utc")
  local prev = recentScans[key]
  recentScans[key] = now
  if prev and (now - prev) < 500 then
    return false
  end
  return true
end

local function matchesEventType(scan, eventName)
  if eventName == "ic_card_scanned" then return isICCardScan(scan) end
  if eventName == "ticket_scanned" then return not isICCardScan(scan) end
  return true
end

local function processInspectionEvent(eventName, ev)
  local payloadScan, payloadSide = parseTicketScannedArgsPacked(ev)
  local bindings = collectInspectionBindings(payloadSide, modeBySide, inspection)
  local handled = false

  for _, binding in ipairs(bindings) do
    local scan = readLastScanned(binding.dev)
    if type(scan) == "table" and matchesEventType(scan, eventName) and shouldProcessScan(scan, binding.side, eventName) then
      if eventName == "ic_card_scanned" or isICCardScan(scan) then
        handleICCardScan(scan, binding.side, binding.dev)
      else
        handleScan(scan, binding.side, binding.dev)
      end
      handled = true
    end
  end

  if not handled and type(payloadScan) == "table" and shouldProcessScan(payloadScan, payloadSide, eventName) then
    local payloadDev = nil
    if payloadSide then
      payloadDev = resolveInspection(payloadSide)
    elseif #bindings == 1 then
      payloadDev = bindings[1].dev
    elseif not modeBySide then
      payloadDev = inspection
    end
    if eventName == "ic_card_scanned" or isICCardScan(payloadScan) then
      handleICCardScan(payloadScan, payloadSide, payloadDev)
    else
      handleScan(payloadScan, payloadSide, payloadDev)
    end
  end
end

local versionTimer = os.startTimer(VERSION_CHECK_INTERVAL)

-- 启动时不再自动触发更新检查逻辑，等待事件触发
-- if pendingGateUpdate and isGateIdleForUpdate() then
--   isUpdating = true
--   drawReadyScreen()
--   os.sleep(0.5)
--   local ok, updated = pcall(runSilentGateUpdate)
--   if ok and updated then
--     os.reboot()
--     return
--   end
--   isUpdating = false
--   drawReadyScreen()
-- end

while true do
  local ev = pack(os.pullEvent())
  if ev[1] == "ticket_scanned" or ev[1] == "ic_card_scanned" then
    markGateBusy(GATE_OPEN_SECONDS + 2)
    processInspectionEvent(ev[1], ev)
    os.sleep(0.35)
    drawReadyScreen()
  elseif ev[1] == "trigger_update" then
    pendingGateUpdate = true
  elseif ev[1] == "timer" and ev[2] == versionTimer then
    pcall(function()
      refreshRemoteLuaVersion(guessBaseFromStatusURL(serverURL))
    end)
    if remoteForceUpdate then
      if expectedGateVersion and expectedGateVersion ~= getLastUpdateVersion() and expectedGateVersion ~= failedUpdateVersion then
        pendingGateUpdate = true
      end
    end

    if pendingGateUpdate and isGateIdleForUpdate() then
      isUpdating = true
      lastUpdateError = nil
      drawReadyScreen()
      os.sleep(0.5)
      local ok, updated = pcall(runSilentGateUpdate)
      if ok and updated == true then
        if expectedGateVersion then setLastUpdateVersion(expectedGateVersion) end
        os.reboot()
        return
      end
      isUpdating = false
      pendingGateUpdate = false
      lastUpdateError = tostring(updated)
      failedUpdateVersion = expectedGateVersion
    end
    drawReadyScreen()
    versionTimer = os.startTimer(VERSION_CHECK_INTERVAL)
  end
end
