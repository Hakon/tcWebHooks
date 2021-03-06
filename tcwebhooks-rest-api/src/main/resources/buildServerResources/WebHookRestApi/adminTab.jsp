<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>

<bs:refreshable containerId="healthReportContainer" pageUrl="${pageUrl}">       

	        <div class="repoList">
	        <h2 class="noBorder">WebHook REST API</h2>
	        <h3>Health Report</h3>
	        
	        <bs:messages key="apiFixResult"/>
	        
	        <div>
	       	<c:choose>  
	    		<c:when test="${hasFoundIssues}"> 
					<div class="icon_before icon16 attentionRed">Health Status: WARNING. TeamCity REST API plugin files contain JAXB jars.</div>
	         	</c:when>  
	    		<c:when test="${restartRequired}"> 
					<div class="icon_before icon16 attentionRed">Health Status: WARNING. TeamCity restart required after Plugin file cleaning.</div>
	         	</c:when>  
	         	<c:otherwise>  
					<div>Health Status: PASSED. No problematic TeamCity REST API plugin files have been found.</div>
	         	</c:otherwise>  
			</c:choose> 
	        
	        </div>
	        
	        <p>The WebHook REST API extends the TeamCity REST API plugin and uses the JAXB library to read and write to the webhooks templates configuration file. </p>
	        <p>Unfortunately, the  TeamCity REST API comes bundled with an old version of the JAXB libraries. This means that when the WebHooks API tries to write to the <i>webhooks-templates.xml</i> configuration file, it fails with a ClassCastException. See <a href="https://github.com/tcplugins/tcWebHooks/issues/43">issue 43 on github for more details</a>.<p/>
	        <p>This page gives you an indication on whether this problem is present on your TeamCity installation. It also has a tool which attempts to fix any problematic REST API installations.</p>
	        
	        <table class="settings">
	        <tr><th colspan="1" style="text-align: left;padding:0.5em;">Plugin file</th><th colspan=2>ZIP File status</th><th colspan=2>Unpacked status</th></tr>
	        <c:forEach items="${fileResults.values()}" var="foundJar">
	        		<tr><td>
	        			${foundJar.path.toString()}
	        			</td>
	        	<c:choose>
	        		<c:when test="${foundJar.jarInZip}">
						<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;Not cool</td>
						<td><a href="#" onclick="WebHookRestApiHealthStatus.fixPluginFile('${foundJar.path.toString()}')">Fix</a></td>
					</c:when>
					<c:otherwise>
						<td colspan="2" class="icon_before icon16" style="border-color: #ccc; background-color:white;">&nbsp;Cool</td>
					</c:otherwise>
				</c:choose>
				<c:choose>
	        		<c:when test="${foundJar.jarInUnpacked}">
						<td colspan="1" class="icon_before icon16 attentionRed" style="border-color: #ccc; background-color:white;">&nbsp;Not cool</td>
						<td><a href="#" onclick="WebHookRestApiHealthStatus.fixPluginFile('${foundJar.path.toString()}')">Fix</a></td>
					</c:when>
					<c:otherwise>
						<td colspan="2" class="icon_before icon16" style="border-color: #ccc; background-color:white;">&nbsp;Cool</td>
					</c:otherwise>
	        	</c:choose>
	        	
	        </c:forEach>
	        
		    </table>
	        
</bs:refreshable>	        
	        </div>

    <bs:dialog dialogId="fixPluginDialog"
               dialogClass="fixPluginDialog"
               title="Fix REST API Plugin"
               closeCommand="WebHookRestApiHealthStatus.FixPluginDialog.close()">
        <forms:multipartForm id="fixPluginForm"
                             action="/admin/debianRepositoryAction.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return WebHookRestApiHealthStatus.FixPluginDialog.doPost();">

            <table class="runnerFormTable">
                <tr><td>The API ZIP file will be modified and the conflicting jars will be removed from both the ZIP file and the unpacked directory.
                 		This will make changes to the files on your TeamCity server.
                        <div id="apiFixResult"></div>
                </td></tr>
            </table>
            <input type="hidden" id="filePath" name="filePath"/>
            <input type="hidden" name="action" id="FixPluginaction" value="fixPluginFile"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="fixPluginDialogSubmit" label="Fix Plugin File"/>
                <forms:cancel onclick="WebHookRestApiHealthStatus.FixPluginDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>	        
	        