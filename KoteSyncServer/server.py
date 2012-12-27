from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

"""================DB instances================"""
# Google App Engine Datastore это не релационная база данных. Она следует концепциям объектных баз данных,
# потому для всего, что будет записанно в базу данных мы должны создать объект

# Информация о устройстве
class Device(db.Model):
    login = db.StringProperty()
    password = db.StringProperty()
    accessors = db.StringListProperty()

# Текущий адрес и порт устройства
class CurrentAddress(db.Model):
    deviceLogin = db.StringProperty()
    address = db.StringProperty()
    port = db.StringProperty()

"""================Handlers================"""
# В рамках фреймворка Google App Engine, приложение строится по следующему сценарию:
# -определяются все возможные запросы и их параметры
# -для каждого типа запроса пишется обработчик (обработчик это наследник класса webapp.RequestHandler),
#  который формирует некоторый ответ и производит действия, изменяющие состояние хранилища данных

# Во всех обработчик реализованные как GET, так и POST методы. Это сделано для облегчения процесса отладки.

# Обработчик запроса на регистрацию нового устройства
class DeviceRegistrator(webapp.RequestHandler):
    def post(self):
        self.process()

    def get(self):
        self.process()

    def process(self):
        login = self.request.get('login')
        # Пытаемся получить из базы объект с подобным полем login
        alreadyRegistered = db.GqlQuery("SELECT * FROM Device WHERE login = :1", login)
        
        # Если устройство с таким логином уже существует - в качестве ответа отдаем код ошибки
        if alreadyRegistered.count() > 0:
            self.error(409)
            return
        
        # Если устройство с таким именем еще не было зарегистрированно, то создаем новый объект и записываем
        # его в базу
        password = self.request.get('password')
        
        dev = Device(login=login, password=password)
        dev.put()

        # Формируем ответ, сообщающий об успешности операции
        self.response.headers['Content-Type'] = "application/json"        
        self.response.write('{"desc":"Success"}')

# Обработчик запроса на регистрац ю текущего адреса и порта устройства
class CurrentAddressRegistrator(webapp.RequestHandler):
    def post(self):
        self.process()

    def get(self):
        self.process()
    
    def process(self):
        addr = self.request.get("address")
        port = self.request.get("port")
        login = self.request.get("login")
        
        # Пытаемся посмотреть был ли зарегистрирован адрес для данного устройства ранее
        alreadyRegistered = db.GqlQuery("SELECT * FROM CurrentAddress WHERE deviceLogin = :1", login)
        curAddr = alreadyRegistered.get()
        
        # Если был, то изменем объект
        if curAddr:
            curAddr.address = addr
            curAddr.port = port
        else:
            # иначе формируем новый объект
            curAddr = CurrentAddress(address=addr, deviceLogin=login, port=port)
            
        curAddr.put()

# Обработчик запроса на подключение к устройству    
class ConnectListener(webapp.RequestHandler):
    def post(self):
        self.process()
    
    def get(self):
        self.process()
    
    def process(self):
        login = self.request.get("deviceTo")
        devQuery = db.GqlQuery("SELECT * FROM CurrentAddress WHERE deviceLogin = :1", login)
        dev = devQuery.get()
        self.response.headers['Content-Type'] = "application/json"
        self.response.write('{"address":"%(addr)s", "port":"%(port)s"}' % {"addr":dev.address, "port":dev.port})
        
# Обработчик запроса текущего состояния сервера. Это единственный запрос, для которого реализован только 
# GET-метод, посольку это запрос для отладки в браузере и непосредственно с устройства использоваться
# не будет
class InfoHandler(webapp.RequestHandler):
    def get(self):
        self.response.write('Registered devices:<br/>')
        
        devices = db.GqlQuery('SELECT * FROM Device')
        
        for dev in devices:
            self.response.write("login: " + dev.login + " password: " + dev.password + " accessors: ")
            self.response.write("<br/>")
            
        self.response.write('Registered addresses:<br/>')
        addreses = db.GqlQuery('SELECT * FROM CurrentAddress')
        for addr in addreses:
            self.response.write(
                'deviceLogin: %(login)s address: %(addr)s, port: %(port)s'
                 % {'login':addr.deviceLogin, 'addr':addr.address, 'port':addr.port})
            self.response.write("<br/>")

"""======================================================================="""
# Регистрация обработчиков
app = webapp.WSGIApplication(
                             [('/register',   DeviceRegistrator),
                              ('/icanhearon', CurrentAddressRegistrator),
                              ('/connect',    ConnectListener),
                              ('/info',       InfoHandler)],
                              debug=True)

def main():
    run_wsgi_app(app)

if __name__ == "__main__":
    main()
