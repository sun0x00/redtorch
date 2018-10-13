# encoding: UTF-8
import logging as log

log.basicConfig(level=log.INFO,
                format='%(asctime)s - %(filename)s[line:%(lineno)d] - %(levelname)s: %(message)s')

token = '041828389e174b87b908190dcd6d202e'
host = '127.0.0.1'
httpPort = 9099
socketIOPort = 9098
debug = False
