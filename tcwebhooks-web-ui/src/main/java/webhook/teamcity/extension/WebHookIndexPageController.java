package webhook.teamcity.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManager;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;

import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import webhook.teamcity.TeamCityIdResolver;
import webhook.teamcity.extension.bean.WebhookBuildTypeEnabledStatusBean;
import webhook.teamcity.extension.bean.WebhookConfigAndBuildTypeListHolder;
import webhook.teamcity.payload.WebHookPayloadManager;
import webhook.teamcity.settings.WebHookConfig;
import webhook.teamcity.settings.WebHookMainSettings;
import webhook.teamcity.settings.WebHookProjectSettings;


public class WebHookIndexPageController extends BaseController {

	    private final WebControllerManager myWebManager;
	    private final WebHookMainSettings myMainSettings;
	    private SBuildServer myServer;
	    private ProjectSettingsManager mySettings;
	    private PluginDescriptor myPluginDescriptor;
	    private final WebHookPayloadManager myManager;

	    public WebHookIndexPageController(SBuildServer server, WebControllerManager webManager, 
	    		ProjectSettingsManager settings, PluginDescriptor pluginDescriptor, WebHookPayloadManager manager, 
	    		WebHookMainSettings configSettings) {
	        super(server);
	        myWebManager = webManager;
	        myServer = server;
	        mySettings = settings;
	        myPluginDescriptor = pluginDescriptor;
	        myMainSettings = configSettings;
	        myManager = manager;
	    }

	    public void register(){
	      myWebManager.registerController("/webhooks/index.html", this);
	    }

	    @Nullable
	    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
	    	
	        HashMap<String,Object> params = new HashMap<String,Object>();
	        params.put("jspHome",this.myPluginDescriptor.getPluginResourcesPath());
        	params.put("includeJquery", Boolean.toString(this.myServer.getServerMajorVersion() < 7));
	        
	    	if (myMainSettings.getInfoUrl() != null && myMainSettings.getInfoUrl().length() > 0){
	    		params.put("moreInfoText", "<li><a href=\"" + myMainSettings.getInfoUrl() + "\">" + myMainSettings.getInfoText() + "</a></li>");
	    		if (myMainSettings.getWebhookShowFurtherReading()){
	    			params.put("ShowFurtherReading", "ALL");
	    		} else {
	    			params.put("ShowFurtherReading", "SINGLE");
	    		}
	    	} else if (myMainSettings.getWebhookShowFurtherReading()){
	    		params.put("ShowFurtherReading", "DEFAULT");
	    	} else {
	    		params.put("ShowFurtherReading", "NONE");
	    	}
	        
	        if(request.getParameter("projectId") != null){
	        	
	        	SProject project = TeamCityIdResolver.findProjectById(this.myServer.getProjectManager(), request.getParameter("projectId"));
	        	if (project != null){
	        		
			    	WebHookProjectSettings projSettings = (WebHookProjectSettings) 
			    			mySettings.getSettings(project.getProjectId(), "webhooks");
			    	
			        SUser myUser = SessionUser.getUser(request);
			        params.put("hasPermission", myUser.isPermissionGrantedForProject(project.getProjectId(), Permission.EDIT_PROJECT));
			    	
			    	String message = projSettings.getWebHooksAsString();
			    	
			    	params.put("haveProject", "true");
			    	params.put("messages", message);
			    	params.put("projectId", project.getProjectId());
			    	params.put("buildTypeList", project.getBuildTypes());
			    	params.put("projectExternalId", TeamCityIdResolver.getExternalProjectId(project));
			    	params.put("projectName", project.getName());
			    	
			    	logger.debug(myMainSettings.getInfoText() + myMainSettings.getInfoUrl() + myMainSettings.getProxyListasString());
			    	
			    	params.put("webHookCount", projSettings.getWebHooksCount());
			    	params.put("formatList", myManager.getRegisteredFormatsAsCollection());
			    	
			    	if (projSettings.getWebHooksCount() == 0){
			    		params.put("noWebHooks", "true");
			    		params.put("webHooks", "false");
			    	} else {
			    		params.put("noWebHooks", "false");
			    		params.put("webHooks", "true");
			    		//params.put("webHookList", projSettings.getWebHooksAsList());
			    		//params.put("webHookList", projSettings.getWebHooksAsList());
			    		params.put("webHooksDisabled", !projSettings.isEnabled());
			    		params.put("webHooksEnabledAsChecked", projSettings.isEnabledAsChecked());
			    		
			    		List<WebhookConfigAndBuildTypeListHolder> webHookList = new ArrayList<WebhookConfigAndBuildTypeListHolder>();
			    		for (WebHookConfig config : projSettings.getWebHooksAsList()){
			    			WebhookConfigAndBuildTypeListHolder holder = new WebhookConfigAndBuildTypeListHolder(config);
				    		for (SBuildType sBuildType : project.getBuildTypes()){
				    			holder.addWebHookBuildType(new WebhookBuildTypeEnabledStatusBean(
				    													sBuildType.getBuildTypeId(), 
				    													sBuildType.getName(), 
				    													config.isEnabledForBuildType(sBuildType)
				    													)
				    										);
				    		}
				    		webHookList.add(holder);
			    		}
			    		params.put("webHookList", webHookList);
			    	}
		    	} else {
		    		params.put("haveProject", "false");
		    		params.put("errorReason", "The project requested does not appear to be valid.");
		    	}
	        } else {
	        	params.put("haveProject", "false");
	        	params.put("errorReason", "No project specified.");
	        }

	        return new ModelAndView(myPluginDescriptor.getPluginResourcesPath() + "WebHook/index.jsp", params);
	    }
}
