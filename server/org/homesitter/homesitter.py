import getopt
from subprocess import check_call, check_output, CalledProcessError
from datetime import datetime
import os
import sys
from pubnub import Pubnub
import sched
import time
from org.homesitter.keys import *


def _callback(message):
    print(message)
    if message.action == "new_picture_request":
        print("New picture request")


def _error(message):
    print(message)


def _connect(message):
    print("\nConnected to " + message)
    pubnub.unsubscribe(CHANNEL)

    # pubnub.state(channel=CHANNEL, state="efrgege")


# def create_file():
#     f = open(file_name, 'w')
#     f.write('answeriefo iejfowiej foiwe oiwj owije oiwfj oiejf ef')
#     f.close()
#     return file_name
#

def unsubscribe():
    print("unsubscribe")
    pubnub.unsubscribe(channel=CHANNEL)


def publish(message):
    pubnub.publish(CHANNEL, message, _callback, _error)


def take_picture():
    file_name = datetime.now().strftime("%d-%m-%Y-%H-%M-%S.jpg")
    try:
        check_call(["fswebcam", "-d", "/dev/video0", "-r", "1280x720", "--title", "Woonkamer", file_name])
        return file_name
        # return "09-09-2016-15-26-49.jpg"
    except FileNotFoundError:
        return ""


def copy_to_remote(file_name):
    remote = "simm@139.59.212.138:/mnt/volume-fra1-01/homesitter/p/"
    check_call(["scp", file_name, remote])


def subscribe():
    print("subscribe")
    pubnub.subscribe(CHANNEL, callback=_callback, error=_error, connect=_connect)


def tick():
    file_name = take_picture()
    copy_to_remote(file_name)
    os.remove(file_name)

    http_link = "http://139.59.212.138/p/" + file_name
    message = {'action': 'new_picture', 'link': http_link}

    publish(message)
    print(http_link)

    scheduler.enter(3.0, 1, tick)
    return file_name

if __name__ == "__main__":
    pubnub = Pubnub(publish_key=PUB_KEY, subscribe_key=SUB_KEY, uuid="home", pres_uuid="home", auth_key=SECRET_KEY)
    subscribe()

    # scheduler = sched.scheduler(time.time, time.sleep)
    #
    # scheduler.enter(3.0, 1, tick)
    # scheduler.run()

    while True:
        print("tick")
        time.sleep(0.5)

    # while True:
    #     i = input("0 unsubscribe, 1 subscribe\n")
    #     if "1" == i:
    #         subscribe()
    #     elif "0" == i:
    #         unsubscribe()
