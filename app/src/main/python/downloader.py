from __future__ import unicode_literals
import youtube_dl
import os.path
import os
import time
from threading import *
from multiprocessing import Process
import sys

class downloader:
    def __init__(self,url, dl_format, directory, max_best_quality, log_path):
        self.url = url
        self.dl_format = dl_format
        self.directory = directory
        self.max_best_quality = max_best_quality
        self.log_path = log_path
        self.fail = False
        self.stop=False
        self.status = 'Stopped'
        print("initialise")
        
    def getTime(self):
        time_stamp = time.localtime()
        return str(time_stamp.tm_mday)+"/"+str(time_stamp.tm_mon)+"/"+str(time_stamp.tm_year)+"::"+str(time_stamp.tm_hour)+":"+str(time_stamp.tm_min)+":"+str(time_stamp.tm_sec)

    def writeLog(self,message,logfile, filename = "/yt.log.txt"):
        message = self.getTime()+"="+message+"\n"
        
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

    def download(self):
        self.status='Preparing'
        self.process = Thread(target=self.downloadAction,args=(self.directory,self.dl_format, self.max_best_quality,self.url))
        
        print('init')
        if os.path.exists(self.log_path+"/yt.log.txt"):
            os.remove(self.log_path+"/yt.log.txt")
        try:
            self.process.start()
            print('start')
            return True
        except Exception as e:
            print('Erreur')
            sys.stdout.write(str(e))
            if "not a valid URL" in str(e):
                self.writeLog("ERROR :"+str(e), self.log_path)
                return False
            time.sleep(10)
            self.writeLog("PY WARNING : first exception :"+str(e)+", wait 10s and retry", self.log_path)
            return False
    def downloadAction(self,directory,dl_format,max_best_quality,url):
        sys.stdout.write("process started")
        ydl_opts = {}
        if directory[-1] != "/":    
            directory+="/"
        if dl_format == "mp4":
            ydl_opts = {
                'format': 'bestvideo[height<='+max_best_quality+']+bestaudio/best[height<='+max_best_quality+']',
                'outtmpl': directory+"%(title)s.%(ext)s",
                'progress_hooks': [self.my_hook],
            }
        elif dl_format == "m4a":
            ydl_opts = {
                'format': 'bestaudio[ext=m4a]/best[ext=m4a]',
                'outtmpl': directory+"%(title)s.%(ext)s",
                'progress_hooks': [self.my_hook],
            }
        try:
            with youtube_dl.YoutubeDL(ydl_opts) as ydl:
                ydl.download([url])
        except ValueError:
            pass
        except Exception as e:
            self.fail = True
        sys.stdout.write("process endeed")
            
    def my_hook(self,d):
        if d['status'] == 'finished':
            file_tuple = os.path.split(os.path.abspath(d['filename']))
            print("Done downloading {}".format(file_tuple[1]))
            self.status='convert'
            if self.stop==True:
                self.process.terminate()
        if d['status'] == 'downloading':
            print(d['filename'], d['_percent_str'], d['_eta_str'])
            self.status=d['filename'].split("/")[-1]+' : '+d['_percent_str']
            print(self.stop)
            if self.stop==True:
                self.status='Stopped'
                print(self.process)
                raise ValueError('Stopping process')
            
    def state(self):
        return self.process.is_alive()
    def stop(self):
        sys.stdout.write('Stopping')
        self.stop=True


