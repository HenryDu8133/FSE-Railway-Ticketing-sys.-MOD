
local URL_MACHINE = "http://gitea.fse-media.group/Henry_Du/FSE-Ticket.sys/raw/branch/main/ticketmachine.lua"

local function writeFile(path, content, binary)
  local mode = binary and "wb" or "w"
  local f = fs.open(path, mode)
  if not f then return false end
  f.write(content)
  f.close()
  return true
end

local function atomicWrite(path, content, binary)
  local tmp = path .. ".new"
  if fs.exists(tmp) then fs.delete(tmp) end
  if not writeFile(tmp, content, binary) then return false end
  if fs.exists(path) then fs.delete(path) end
  fs.move(tmp, path)
  return true
end

local function httpGet(url)
  if not http then return false, "HTTP API disabled" end
  local okReq, err = pcall(function()
    http.request({ url = url, method = "GET" })
  end)
  if not okReq then return false, tostring(err) end

  while true do
    local ev, p1, p2, p3 = os.pullEvent()
    if ev == "http_success" and p1 == url then
      local res = p2
      if type(res) == "table" and type(res.readAll) == "function" then
        local data = res.readAll()
        res.close()
        return true, data
      end
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
        return false, data
      end
      return false, tostring(err or "http_failure")
    end
  end
end

term.clear()
term.setCursorPos(1, 1)
print("Ticket Machine Updater")
print("")
print("Downloading ticket machine program...")

local ok, code = httpGet(URL_MACHINE)
if not ok or type(code) ~= "string" or #code == 0 then
  print("Download failed: " .. tostring(code or ""))
  return
end

if not atomicWrite("startup.lua", code, false) then
  print("Write failed: startup.lua")
  return
end
atomicWrite("startup", code, false)
if fs.exists("ticketmachine.lua") then atomicWrite("ticketmachine.lua", code, false) end

print("")
print("Done.")
print("Reboot the computer to apply the update.")
