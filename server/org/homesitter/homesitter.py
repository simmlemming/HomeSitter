from pubnub import Pubnub
from org.homesitter.keys import *


def _callback(message):
    print(message)


def _error(message):
    print(message)


def _connect(message):
    print("\nConnected to " + message)

if __name__ == "__main__":
    pubnub = Pubnub(publish_key=PUB_KEY, subscribe_key=SUB_KEY, uuid="watcher", pres_uuid="watcher", auth_key=SECRET_KEY)
    pubnub.subscribe(CHANNEL, callback=_callback, error=_error, connect=_connect)
    # pubnub.here_now(CHANNEL, callback=_callback, error=_error, uuids=True, state=True)

    # input("Press 'Enter' any key to exit")
    # unsubscribe = pubnub.unsubscribe(channel=CHANNEL)
    # print("unsubscribed")
