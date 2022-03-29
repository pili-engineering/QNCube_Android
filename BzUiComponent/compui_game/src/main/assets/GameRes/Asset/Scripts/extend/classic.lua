local Object = {}
Object.__index = Object

function Object:new()
end

function Object:extend()
  local classtype = {}
  for k, v in pairs(self) do
    if k:find("__") == 1 then
      classtype[k] = v
    end
  end
  classtype.__index = classtype
  classtype.super = self
  self.mixins = {}
  setmetatable(classtype, self)
  return classtype
end

function Object:implement(...)
  for _, classtype in pairs({...}) do
    for k, v in pairs(classtype) do
      if self[k] == nil and type(v) == "function" then
        self[k] = v
      end
    end
  end
end


function Object:is(T)
  local mt = getmetatable(self)
  while mt do
    if mt == T then
      return true
    end
    mt = getmetatable(mt)
  end
  return false
end


function Object:__tostring()
  return "Object"
end


function Object:__call(...)
  local object = setmetatable({}, self)
  object:new(...)
  return object
end


return Object
