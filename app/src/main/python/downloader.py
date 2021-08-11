from __future__ import unicode_literals
import youtube_dl
import ffmpeg

class MyLogger(object):
    def debug(self, msg):
        print(msg)

    def warning(self, msg):
        print(msg)

    def error(self, msg):
        print(msg)


def my_hook(d):
    if d['status'] == 'finished':
        print('Done downloading, now converting ...')




def download(url, dl_format, directory, max_best_quality):
    try:
        ydl_opts = {}
        if directory[-1] != "/":
            directory+="/"
        if dl_format == "mp4":
            ydl_opts = {
                'format': 'bestvideo[height<='+max_best_quality+']+bestaudio/best[height<='+max_best_quality+']',
                'logger': MyLogger(),
                'progress_hooks': [my_hook],
                'outtmpl': directory+"%(title)s.%(ext)s"
            }
        elif dl_format == "m4a":
            ydl_opts = {
                'format': 'bestaudio[ext=m4a]/best[ext=m4a]',
                'logger': MyLogger(),
                'progress_hooks': [my_hook],
                'outtmpl': directory+"%(title)s.%(ext)s"
            }
        print(ydl_opts)
        with youtube_dl.YoutubeDL(ydl_opts) as ydl:
            ydl.download([url])
        return True
    except Exception as e:
        print(e)
        return False
