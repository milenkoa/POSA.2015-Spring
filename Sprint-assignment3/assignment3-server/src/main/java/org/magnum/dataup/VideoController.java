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
import org.magnum.dataup.model.VideoRating;
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
import org.apache.log4j.Logger;


@Controller
public class VideoController {
	
	private Logger logger = Logger.getLogger(VideoController.class);

	private HashMap<Long, Video> videos = new HashMap<Long, Video>();
	private HashMap<Long, VideoRating> ratings = new HashMap<Long, VideoRating>();
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
		logger.info("--> getVideoList() <--");		
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
		logger.info("--> getVideo(), ID: " + Long.toString(id));
		
		Video v;
		videoDataMgr = VideoFileManager.get();
		if (videos.containsKey(id)) {
			v = videos.get(id);
			videoDataMgr.copyVideoData(v, response.getOutputStream());
			response.setContentType(v.getContentType());
			logger.info("<-- getVideo(), OK");
		}
		else {
			response.setStatus(404);
			logger.info("<-- getVideo(), Failed to find ID: " + Long.toString(id));
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
		logger.info("--> addVideo(), Video: " + v.toString());
		
		/* set ID */
		checkAndSetId(v);
		
		/* generate DATA URL */
		String dataUrl = new StringBuilder().append(getUrlBaseForLocalServer()).append("/video/").append(v.getId()).append("/data").toString();
		v.setDataUrl(dataUrl);
				
		/* add video metadata to repository */
		videos.put(v.getId(), v);
		
		/* create video rating */
		ratings.put(v.getId(), new VideoRating());
		
		logger.info("<-- addVideo(), OK, ID: " + v.getId());
		return v;
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *  METHOD: rateVideo
	 *  Implements: POST /video
	 *  
	 *  
	 */
	@RequestMapping (value="/video/{id}/rate", method=RequestMethod.POST)
	public @ResponseBody Video rateVideo(@PathVariable("id") long id, @RequestParam("rating") int rating, HttpServletResponse response) {
		logger.info("--> rateVideo(), ID: " + Long.toString(id) + ", rating: " + rating);
		
		if (rating < 1 || rating > 5) {
			response.setStatus(400);
			logger.info("<-- rateVideo(), wrong parameter received");
			return null;
		}
		
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			VideoRating videoRating = ratings.get(id);
				
			int currentRating = videoRating.getRating();
			int currentNums = videoRating.getNums();
			if (currentNums == 0) {
				currentRating = rating;
				currentNums++;
			}
			else {
				currentRating = (currentRating+rating)/2;
				currentNums++;
			}
			videoRating.setRating(currentRating);
			videoRating.setNums(currentNums);
			v.setRating(currentRating);
			//v.updateVideoRating(rating);
			logger.info("<-- rateVideo(), ID: " + id + ", rating " + videoRating);
			return v;			
		}
		else {
			logger.info("<-- rateVideo(), Failed to find ID: " + id);
			response.setStatus(404);
			return null;			
		}		
	}
	
	/**                                                                                                                                                                                                                                                                        
	 *  METHOD: setVideoData
	 *  Implements: POST /video/{id}/data
	 *  
	 *  Uploads binary mpeg data provided as multipart request
	 */
	@RequestMapping (value="/video/{id}/data", method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData, HttpServletResponse response) throws IOException {
		logger.info("--> setVideoData(), ID: " + Long.toString(id));
		
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			InputStream in = videoData.getInputStream();
			videoDataMgr = VideoFileManager.get();
			videoDataMgr.saveVideoData(v, in);
			logger.info("<-- setVideoData(), READY");
			return new VideoStatus(VideoState.READY);
		}
		else {
			logger.info("setVideoData() <-- Failed to find ID: " + Long.toString(id));
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
