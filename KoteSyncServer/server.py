from google.appengine.ext import webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext import db

"""================DB instances================"""

class Device(db.Model):
    login = db.StringProperty()
    password = db.StringProperty()
    accessors = db.StringListProperty()

class CurrentAddress(db.Model):
    deviceLogin = db.StringProperty()
    address = db.StringProperty()
    port = db.StringProperty()

"""================Handlers================"""
class DeviceRegistrator(webapp.RequestHandler):
    def post(self):
        self.process()

    def get(self):
        self.process()

    def process(self):
        login = self.request.get('login')
        alreadyRegistered = db.GqlQuery("SELECT * FROM Device WHERE login = :1", login)
        
        if alreadyRegistered.count() > 0:
            self.error(409)
            return
        
        password = self.request.get('password')
        
        dev = Device(login=login, password=password)
        dev.put()
        
        self.response.write('{desc:"Success"}')

class CurrentAddressRegistrator(webapp.RequestHandler):
    def post(self):
        self.process()

    def get(self):
        self.process()
    
    def process(self):
        addr = self.request.get("address")
        port = self.request.get("port")
        login = self.request.get("login")
        
        alreadyRegistered = db.GqlQuery("SELECT * FROM CurrentAddress WHERE deviceLogin = :1", login)
        curAddr = alreadyRegistered.get()
        
        if curAddr:
            curAddr.address = addr
            curAddr.port = port
        else:
            curAddr = CurrentAddress(address=addr, deviceLogin=login, port=port)
            
        curAddr.put()
    
class ConnectListener(webapp.RequestHandler):
    def post(self):
        self.process()
    
    def get(self):
        self.process()
    
    def process(self):
        login = self.request.get("deviceTo")
        devQuery = db.GqlQuery("SELECT * FROM CurrentAddress WHERE deviceLogin = :1", login)
        dev = devQuery.get()
        self.response.write('{address:"%(addr)s", port:"%(port)s"}' % {"addr":dev.address, "port":dev.port})
        
        
class Dispatcher(webapp.RequestHandler):
    def get(self):
        pass

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

class GiveAccess(webapp.RequestHandler):
    def post(self):
        self.process()
    
    def get(self):
        self.process()
    
    def process(self):
        login = self.request.get('login')
        device = self.request.get('device')
        
        deviceQuery = db.GqlQuery("SELECT * FROM Device WHERE login = :1", login)
        dev = deviceQuery.get()
        dev.accessors.add(device)
        dev.put()

"""======================================================================="""
app = webapp.WSGIApplication(
                             [('/register',   DeviceRegistrator),
                              ('/icanhearon',  CurrentAddressRegistrator),
                              ('/connect',    ConnectListener),
                              ('/ilistenyou', Dispatcher),
                              ('/giveaccess', GiveAccess),
                              ('/info',       InfoHandler)],
                              debug=True)

def main():
    run_wsgi_app(app)

if __name__ == "__main__":
    main()
