import os
import time
from threading import *
import sys
from pytube import YouTube, Playlist,exceptions

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
        self.cmd_str = [""]
        self.res_list = ['2160p', '1440p', '1080p', '720p', '480p', '360p', '240p', '144p']
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
        self.status='Preparing...'
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
            self.fail = True

    def download_video(self,video,directory,max_best_quality):
                self.streams = video.streams
                self.mp4streams = self.streams.filter(progressive=True,res=self.max_best_quality)
                if len(self.mp4streams) == 0:
                    for i in range(len(self.res_list)):
                        if i>self.res_list.index(self.max_best_quality):
                            resolution = self.res_list[i]
                            sys.stdout.write(resolution)
                            self.mp4streams = self.streams.filter(progressive=True,res=resolution)
                        if len(self.mp4streams) != 0:
                            break
                    if len(self.mp4streams) == 0:
                        sys.stdout.write("No streams")
                        raise Exception("No streams")
                sys.stdout.write(str(len(self.mp4streams)))
                self.download_stream = self.mp4streams.order_by('fps')[-1]
                self.downloadfilename = directory+self.download_stream.default_filename
                self.download_stream.download(filename=directory+self.download_stream.default_filename)
                
    def download_audio(self,video,directory):
        self.streams = video.streams
        self.streams = self.streams.filter(only_audio=True).order_by('abr')[::-1]
        self.downloadfilename = directory
        sys.stdout.write(directory)
        self.downloadfilename += self.streams[-1].default_filename
        self.status='downloading...'
        sys.stdout.write('downloading')
        
        out = self.streams[-1].download(filename=self.downloadfilename)
        sys.stdout.write(out)
        self.cmd_str = ['-i',self.downloadfilename,'-map','0','-c','copy',self.downloadfilename.split('.')[0]+'.m4a']
        
        
        
    def downloadAction(self,directory,dl_format,max_best_quality,url):
        sys.stdout.write("process started")
        try:
            sys.stdout.write(str(os.access(directory, os.W_OK))) 
            if directory[-1] == "/":    
                directory == directory[:-2]
            if dl_format == "mp4":
                if not 'list' in url:
                    video = YouTube(self.url,on_progress_callback=self.progress_handler)
                    result = self.download_video(video,directory,max_best_quality)
                    self.fail = result
                elif 'list' in url:
                    self.playlist = Playlist(url)
                    for i in range(len(self.playlist.videos)):
                        video = self.playlist.videos[i]
                        self.status = str(round((i+1)/len(self.playlist.videos)*100,0))+"%"
                        video.register_on_progress_callback(self.progress_handler)
                        self.download_video(video,directory,max_best_quality)
                                    
            elif dl_format == "m4a":
                if not 'list' in url:
                    video = YouTube(self.url,on_progress_callback=self.progress_handler)
                    self.download_audio(video,directory)
                    
                elif 'list' in url:
                    temp_cmd = []
                    self.playlist = Playlist(url)
                    for i in range(len(self.playlist.videos)):
                        video = self.playlist.videos[i]
                        self.status = str(round((i+1)/len(self.playlist.videos)*100,0))+"%"
                        video.register_on_progress_callback(self.progress_handler)
                        self.download_audio(video,directory)
                        temp_cmd.append(self.cmd_str.copy())
                    input_cmd = []
                    output_cmd = []
                    for cmd in temp_cmd:
                        input_cmd.append(cmd[0])
                        input_cmd.append(cmd[1])
                        output_cmd.append(cmd[2])
                        output_cmd.append(str(temp_cmd.index(cmd))+':a')
                        for i in range(4,len(cmd)):
                            output_cmd.append(cmd[i])
                    
                    self.cmd_str = []
                    for i in input_cmd:
                        self.cmd_str.append(i)
                    for i in output_cmd:
                        self.cmd_str.append(i)
                    sys.stdout.write(' '.join(self.cmd_str))    
            self.status = 'converting...'
            sys.stdout.write("process endeed")
        except Exception as e:
            self.fail = True
            self.writelog(str(e),self.log_path)        
    def progress_handler(self,stream,chunck,remaining):
        size = stream.filesize
        downloaded = size-remaining
        self.status=str(round(downloaded/size*100,0))+"%"
        if self.stop == True:
            try:
                os.remove(self.downloadfilename)
                self.status = 'Stopping...'
            except:
                pass
            self.fail = True
            raise exceptions.PytubeError()

    def cmd(self):
        return self.cmd_str
    def state(self):
        return self.process.is_alive()
    def stop(self):
        self.stop=True
        self.process.terminate()


