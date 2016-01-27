##获取音频列表及音频播放记录
create or replace view v_c_audio as 
select (select count(0) from sys_log_info a where a.PID=t.PID and a.ACTTYPE = 'INFO' and a.LOGTYPE = 'REQUEST' and locate(t.AUDIOID,a.CONTENT) > 0) PLAYCOUNT, 
(select c.RESPATH from sys_reslib c where c.RESID = t.IMGPATHID  and c.PID = t.PID) PICPATH,
(select d.RESPATH from sys_reslib d where d.RESID = t.THUMBNAILPATHID  and d.PID = t.PID) THUMBNAILPATH,
t.* from c_audio t;

##获取视频列表及视频播放记录
create or replace view v_c_video as 
select (select count(0) from sys_log_info a where a.PID=t.PID and a.ACTTYPE = 'INFO' and a.LOGTYPE = 'REQUEST' and locate(t.VIDEOID,a.CONTENT) > 0) PLAYCOUNT,
(select c.RESPATH from sys_reslib c where c.RESID = t.IMGPATHID  and c.PID = t.PID) PICPATH,
(select d.RESPATH from sys_reslib d where d.RESID = t.THUMBNAILPATHID  and d.PID = t.PID) THUMBNAILPATH,
t.* from c_video t;

##获取视频地址列表
create or replace view v_c_wolfvideores as 
select ID,BOOKID,PID,VIDEOID,TITLE,CLIENTTYPE,(select respath from sys_reslib t where t.RESID=s.videopathid) VIDEOPATH from c_video s;
##获取音频地址列表
create or replace view v_c_wolfaudiores as 
select ID,BOOKID,PID,AUDIOID,TITLE,CLIENTTYPE,(select respath from sys_reslib t where t.RESID=s.audiopathid) AUDIOPATH from c_audio s;
#轮播图片列表
create or replace view  v_c_banner as 
select t.*,
(select RESPATH from sys_reslib b where RESTYPE='PIC' and b.RESID=t.IMGPATHID) IMGPATH,
 (select RESPATH from sys_reslib b where RESTYPE='LINK' and b.RESID=t.LINKRESID) LINKPATH from c_banner t 