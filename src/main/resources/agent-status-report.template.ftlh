<style>
	.main-container {
		margin-top: 0;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker-elastic-agent {
        padding: 70px 20px 0 20px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tabs {
        position: relative;
        clear: both;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tabs .tab {
        float: left;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tabs .tab .content {
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tab label {
        background-color: #d1c4e9;
        padding: 7px 15px;
        line-height: 19px;
        font-size: 13px;
        border-radius: 3px 3px 0 0;
        font-weight: 600;
        color: #333;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tab-content .logs {
        background-color: #383838;
        font-size: 13px;
        font-family: monaco;
        color: white;
        display: block;
        font-weight: 400;
        padding-left: 10px;
        height: calc(100vh - 290px);
        max-height: calc(100vh - 290px);
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tab-content {
        margin-top: 0;
        height: calc(100vh - 280px);
        overflow-x: hidden;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .tab-content-outer {
        background: white;
        padding: 20px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .warning-message {
        background-color: #fdf5e2;
        color: #b88c34;
        font-weight: 600;
        font-size: 15px;
        padding: 10px 20px;
        border-radius: 3px;
        margin: 0 0 1rem 0;
        border: 1px solid rgba(10, 10, 10, 0.25);
        position: relative;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .entity_title span.label {
        position: absolute;
        margin-top: -10px;
        font-size: 0.625em;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .status-report-page-header {
        width: 100%;
        height: 50px;
        z-index: 10;
        position: fixed;
        background: #fff;
        box-shadow: 0 0 0 #000;
        border-bottom: 1px solid #ddd;
        background-size: cover;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .status-report-page-header .entity_title {
        top: 12px;
        left: 20px;
        position: absolute;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .status-report-page-header .entity_title .last h1 {
        margin-top: 2px !important;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .status-report-page-header .entity_title li a {
        font-size: 0.9375rem;
        font-weight: 600;
        line-height: normal;
    }

    [data-plugin-style-id="docker-elastic-plugin"] textarea[readonly="readonly"], textarea[readonly] {
        cursor: auto;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value {
        float: none;
        list-style-type: none;
        clear: both;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair {
        float: none;
        display: flex;
        border-bottom: 1px dotted #ddd;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair label {
        width: 225px;
        padding: 5px 10px 5px 0px;
        min-width: 150px;
        word-wrap: break-word;
        font-size: 13px;
        font-weight: 600;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair span {
        padding: 5px 10px 5px 0px;
        font-size: 13px;
        line-height: 20px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair pre {
        line-height: 2.5;
    }

    [data-plugin-style-id="docker-elastic-plugin"] table {
        text-align: left;
        border-spacing: 0;
        border-collapse: collapse;
    }

    [data-plugin-style-id="docker-elastic-plugin"] table thead tr th {
        font-weight: 600;
        border-bottom: 2px solid #ddd;
        border-top: 1px solid #ddd;
    }

    [data-plugin-style-id="docker-elastic-plugin"] no-padding-left {
        padding: 8px 8px 8px 0px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair table tr th,
    [data-plugin-style-id="docker-elastic-plugin"] .name-value_pair table tr td {
        border-bottom: 1px dotted #ddd;
    }

    [data-plugin-style-id="docker-elastic-plugin"] table tr th,
    [data-plugin-style-id="docker-elastic-plugin"] table tr td {
        padding: 8px;
        font-size: 13px;
        border-top: 0;
        line-height: 1.42857143;
        border-bottom: 1px solid #ddd;
        vertical-align: top;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .task-status {
        display: inline-block;
        width: 10px;
        height: 10px;
        margin-top: 6px;
        margin-right: 5px;
        background-color: #FFEB3B;
        border-radius: 100%;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .running
    [data-plugin-style-id="docker-elastic-plugin"] .complete {
        background-color: #4CAF50;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .failed,
    [data-plugin-style-id="docker-elastic-plugin"] .rejected {
        background-color: #F44336;
    }

    [data-plugin-style-id="docker-elastic-plugin"] td.message {
        padding: 5px;
    }
</style>

<div data-plugin-style-id="docker-elastic-plugin">
    <div class="status-report-page-header">
        <ul class="entity_title">
            <li class="name">
                <span class="label">Pipeline</span>
                <a href="/go/tab/pipeline/history/${jobIdentifier.pipelineName!''}"
                   title="View this pipeline's activity"
                   rel="nofollow noreferrer" target="_blank">${jobIdentifier.pipelineName!''}</a>
            </li>
            <li class="pipeline_label">
                <span class="label">Instance</span>
                <span class="run_no">${jobIdentifier.pipelineCounter!''}</span>
                <a href="/go/pipelines/value_stream_map/${jobIdentifier.pipelineName!''}/${jobIdentifier.pipelineCounter!''}"
                   title="View this stage's jobs summary"
                   rel="nofollow noreferrer" target="_blank">VSM</a>
            </li>
            <li class="stage_name">
                <span class="label">Stage</span>
                <a
                        href="/go/pipelines/${jobIdentifier.pipelineName!''}/${jobIdentifier.pipelineCounter!''}/${jobIdentifier.stageName!''}/${jobIdentifier.stageCounter!''}"
                        title="View this stage's details"
                        rel="nofollow noreferrer" target="_blank">${jobIdentifier.stageName!''}
                    / ${jobIdentifier.stageCounter!''}</a>
            </li>
            <li class="job_label">
                <span class="label">Job</span>
                <a
                        href="/go/tab/build/detail/${jobIdentifier.representation!''}"
                        title="View this job's details"
                        rel="nofollow noreferrer" target="_blank">${jobIdentifier.jobName!''}</a>
            </li>

            <li class="last">
                <span class="label">Elastic Agent Id</span>
                <h1>${ elasticAgentId !}</h1>
            </li>
        </ul>
    </div>

    <div class="content_wrapper_inner docker-elastic-agent">
        <div class="row">
            <div class="sub_tabs_container">
                <ul class="tabs" ng-init="currenttab=currentab || 'service-details'">
                    <li ng-class="{'current_tab' : currenttab == 'service-details' }"
                        ng-click="currenttab='service-details'">
                        <a href="#">Details</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'service-logs' }" ng-click="currenttab='service-logs'">
                        <a href="#">Logs</a>
                    </li>
                    <li ng-class="{'current_tab' : currenttab == 'environments' }" ng-click="currenttab='environments'">
                        <a href="#">Environments</a>
                    </li>
                </ul>
            </div>

            <div class="tab-content-outer">
                <div class="tab-content" ng-show="currenttab == 'service-details'">
                    <ul class="name-value">
                        <li class="name-value_pair">
                            <label>Agent ID</label>
                            <span>${ elasticAgentId !}</span>
                        </li>
                        <li class="name-value_pair">
                            <label>Created At</label>
                            <span>{{ ${createdAt?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</span>
                        </li>
                        <li class="name-value_pair">
                            <label>Image</label>
                            <span>${image !}</span>
                        </li>
                        <li class="name-value_pair">
                            <label>Command</label>
                            <span>${command !}</span>
                        </li>
                        <li class="name-value_pair">
                            <label>IP Address</label>
                            <span>${ipAddress!}</span>
                        </li>
                        <li class="name-value_pair">
                            <label>Extra Hosts</label>
                            <span>
                                <#if hosts?? && hosts?size != 0>
                                    <#list hosts as host>
                                        ${host}<br/>
                                    </#list>
                                <#else>
                                    Not Specified
                                </#if>
                            </span>
                        </li>
                    </ul>
                    <br/><br/>
                </div>
                <div class="tab-content environments" ng-show="currenttab == 'environments'">
                    <ul class="name-value">
						<#list environmentVariables?keys as name>
                            <li class="name-value_pair">
                                <label style="width: 300px;">${name!}</label>
                                <span>${environmentVariables[name]!}</span>
                            </li>
                        </#list>
                    </ul>
                </div>
                <div class="tab-content service-logs" ng-show="currenttab == 'service-logs'">
					<#if logs?? && logs?has_content>
                        <textarea readonly class="logs">${ logs! }</textarea>
                    <#else>
						<h3>Logs not available for this agent.</h3>
                    </#if>
                </div>
            </div>
        </div>
    </div>
</div>


