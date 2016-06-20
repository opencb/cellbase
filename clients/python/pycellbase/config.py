class ConfigClient(object):
    def __init__(self):

        # Initializing default parameters for the database
        self._host = 'bioinfo.hpc.cam.ac.uk'
        self._port = '80'
        self._version = 'latest'

    def _check_host(self):
        # TODO Check host availability; Choose another if needed.
        pass

    @property
    def version(self):
        return self._version

    @version.setter
    def version(self, new_version):
        self._version = new_version

    @property
    def host(self):
        return self._host

    @host.setter
    def host(self, new_host):
            self._host = new_host

    @property
    def port(self):
        return self._port

    @port.setter
    def port(self, new_port):
            self._port = new_port
