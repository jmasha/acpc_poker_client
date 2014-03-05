#! /urs/bin/python

def connectSocket(ip,port):
    ''' 
    IP and PORT are the IP and the PORT of the pythonAAAIInterface
    '''
    import socket
    playerSocket = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
    playerSocket.connect((ip,int(port)))
    playerSocket.send('Bot:1000:1:1\n')
    return playerSocket

def listenSocket(playerSocket):
    while 1:
        gameState = playerSocket.recv(512)
        print "RECIEVED:" , gameState
        action = gameState.strip('\r\n')+":"+sendAction()
        
        print "SENDING:", action
        playerSocket.send(action+'\r\n')

def sendAction():
    choice  = random.randint(2,3)
    if choice == 1:
        return 'f'
    elif choice >= 2:
        return 'c'
    else:
        return 'r50'

if __name__ ==  "__main__":
    import sys, random
    args = sys.argv
    if(len(args)<3):
        print "useage python pythonchump.py IP PORT"
        sys.exit()
    ip = args[1]
    port = args[2]
    playerSocket = connectSocket(ip,port)
    listenSocket(playerSocket)
