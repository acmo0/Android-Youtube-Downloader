from __future__ import unicode_literals
import youtube_dl
import os.path
import time

def getTime():
    time_stamp = time.localtime()
    return str(time_stamp.tm_mday)+"/"+str(time_stamp.tm_mon)+"/"+str(time_stamp.tm_year)+"::"+str(time_stamp.tm_hour)+":"+str(time_stamp.tm_min)+":"+str(time_stamp.tm_sec)

def writeLog(message,logfile, filename = "/yt.log.txt"):
    message = getTime()+"="+message+"\n"
    
    if not os.path.exists(logfile):
        try:
            os.mkdir(logfile)
            with open(logfile+filename, 'w+') as lf:
                lf.write(message)
        except Exception as e:
            print(e)
    else:
        try:
            with open(logfile+filename, "a") as lf:
                lf.write(message)
        except Exception as e:
            print(e)


def downloadAction(url, dl_format, directory, max_best_quality):
    ydl_opts = {}
    if directory[-1] != "/":
        directory+="/"
    if dl_format == "mp4":
        ydl_opts = {
            'format': 'bestvideo[height<='+max_best_quality+']+bestaudio/best[height<='+max_best_quality+']',
            'outtmpl': directory+"%(title)s.%(ext)s"
        }
    elif dl_format == "m4a":
        ydl_opts = {
            'format': 'bestaudio[ext=m4a]/best[ext=m4a]',
            'outtmpl': directory+"%(title)s.%(ext)s"
        }
    print(ydl_opts)
    with youtube_dl.YoutubeDL(ydl_opts) as ydl:
        ydl.download([url])

def download(url, dl_format, directory, max_best_quality, log_path):
    if os.path.exists(log_path+"/yt.log.txt"):
        os.remove(log_path+"/yt.log.txt")
    try:

        downloadAction(url, dl_format, directory, max_best_quality)
        return True
    except Exception as e:
        if "not a valid URL" in str(e):
            writeLog("ERROR :"+str(e), log_path)
            return False
        time.sleep(10)
        writeLog("PY WARNING : first exception :"+str(e)+", wait 10s and retry", log_path)
        try:
            downloadAction(url, dl_format, directory, mac_best_quality)
            return True
        except Exception as e:
            writeLog("PY ERROR :"+str(e), log_path)
            print(e)
            return False
