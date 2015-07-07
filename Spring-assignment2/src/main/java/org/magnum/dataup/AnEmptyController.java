/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
//import org.magnum.dataup.VideoSvcApi.VIDEO_SVC_PATH;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class AnEmptyController {

	/**
	 * You will need to create one or more Spring controllers to fulfill the
	 * requirements of the assignment. If you use this file, please rename it
	 * to something other than "AnEmptyController"
	 * 
	 * 
		 ________  ________  ________  ________          ___       ___  ___  ________  ___  __       
		|\   ____\|\   __  \|\   __  \|\   ___ \        |\  \     |\  \|\  \|\   ____\|\  \|\  \     
		\ \  \___|\ \  \|\  \ \  \|\  \ \  \_|\ \       \ \  \    \ \  \\\  \ \  \___|\ \  \/  /|_   
		 \ \  \  __\ \  \\\  \ \  \\\  \ \  \ \\ \       \ \  \    \ \  \\\  \ \  \    \ \   ___  \  
		  \ \  \|\  \ \  \\\  \ \  \\\  \ \  \_\\ \       \ \  \____\ \  \\\  \ \  \____\ \  \\ \  \ 
		   \ \_______\ \_______\ \_______\ \_______\       \ \_______\ \_______\ \_______\ \__\\ \__\
		    \|_______|\|_______|\|_______|\|_______|        \|_______|\|_______|\|_______|\|__| \|__|
                                                                                                                                                                                                                                                                        
	 * 
	 */
	private HashMap<Long, Video> videos = new HashMap<Long, Video>();
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	private VideoFileManager videoDataMgr;

	/**                                                                                                                                                                                                                                                                        
	 *  METHOD: getVideoList 
	 *  Implements: GET /video
	 *  
	 *  Returns the list of videos added to the server
	 */
	@RequestMapping (value="/video", method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {		
		return videos.values();
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *	METHOD: getVideo
	 *  Implements: GET /video/{id}/data
	 *  
	 *  Returns binary mpeg data
	 */
	@RequestMapping (value="/video/{id}/data", method=RequestMethod.GET)
	public HttpServletResponse getVideo(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
		Video v;
		videoDataMgr = VideoFileManager.get();
		if (videos.containsKey(id)) {
			v = videos.get(id);
			videoDataMgr.copyVideoData(v, response.getOutputStream());
			response.setContentType(v.getContentType());	
		}
		else {
			response.setStatus(404);	
		}
		return response;
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *  METHOD: addVideo
	 *  Implements: POST /video
	 *  
	 *  Adds video metadata provided in request body as application/json
	 */
	@RequestMapping (value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {	
		/* set ID */
		/*if (v.getId() == 0) {
			v.setId(currentId.incrementAndGet());
		}*/
		checkAndSetId(v);
		
		/* generate DATA URL */
		String dataUrl = new StringBuilder().append(getUrlBaseForLocalServer()).append("/video/").append(v.getId()).append("/data").toString();
		v.setDataUrl(dataUrl);
		
		
		/* add video metadata to repository */
		videos.put(v.getId(), v);
		
		return v;
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *  METHOD: setVideoData
	 *  Implements: POST /video/{id}/data
	 *  
	 *  Uploads binary mpeg data provided as multipart request
	 */
	@RequestMapping (value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) throws IOException {
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			InputStream in = videoData.getInputStream();
			videoDataMgr = VideoFileManager.get();
			videoDataMgr.saveVideoData(v, in);
			return new VideoStatus(VideoState.READY);
		}
		else {
			response.setStatus(404);
			return null;			
		}
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *  PRIVATE METHOD: getUrlBaseForLocalServer
	 *  
	 *  Returns base address of the server
	 */
	private String getUrlBaseForLocalServer() {
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base = 
           "http://"+request.getServerName() 
           + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
     }
	
	/**                                                                                                                                                                                                                                                                        
	 *  PRIVATE METHOD: checkAndSetId
	 *  
	 *  Adds ID to video object provided as parameter
	 */
	private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }
	
	
}
