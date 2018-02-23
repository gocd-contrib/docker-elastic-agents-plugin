<style>

    [data-plugin-style-id="docker-elastic-plugin"] .node-content {
        border: 1px solid #d1c4e9;
        background: #fff;
        border-radius: 2px;
        padding: 10px;
        width: 90%;
        margin: 60px auto;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .page_header {
        display: flex;
        width: 100%;
        justify-content: space-between;
        align-items: center;
        background: #fff;
        box-shadow: 0 0 0 #000;
        height: 50px;
        box-sizing: border-box;
        border-bottom: 1px solid #ccc;
    }


    [data-plugin-style-id="docker-elastic-plugin"] .page_title {
        font-size: 17px;
        font-weight: 600;
        margin: 0;
        align-self: center;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table {
        width: calc(100% - 40px);
        min-width: 95%;
        border: 1px solid #D8D8D8;
        font-size: 13px;
        margin: 10px 20px 20px 20px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table thead {
        background: #D8D8D8;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table thead th {
        padding: 10px 20px;
        font-weight: bold;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table tbody tr {
        border-top: 1px solid #D8D8D8;
        border-bottom: 1px solid #D8D8D8;
        border-collapse: collapse;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table tbody td {
        padding: 10px 20px;
        max-width: 190px;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table tbody td.state {
        width: 50px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table tbody td.task-created {
        width: 160px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker table tbody td.service-id {
        width: 260px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .message {
        font-size: 13px;
        font-weight: 600;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dl.properties {
        overflow: hidden;
        margin: 0;
        font-size: 13px;
        display: inline-block;
        vertical-align: top;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dt {
        float: left;
        clear: both;
        min-width: 150px;
        text-align: right;
        font-weight: normal;
        padding: 5px 0px;
        margin: 0;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dl.properties.inline dt {
        clear: none;
        min-width: auto;
        vertical-align: bottom;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dl.properties.inline dd, [data-plugin-style-id="docker-elastic-plugin"] dl.properties.inline dt {
        padding: 0px;
        height: 15px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dl.properties.inline dd {
        margin-right: 15px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .node-header .hostname, [data-plugin-style-id="docker-elastic-plugin"] .node-header .node-id {
        min-width: 190px;
        max-width: 190px;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    @media screen and (max-width: 1366px) {
        [data-plugin-style-id="docker-elastic-plugin"] .node-header .hostname, [data-plugin-style-id="docker-elastic-plugin"] .node-header .node-id {
            max-width: 150px;
            min-width: 150px;
            overflow: hidden !important;
            text-overflow: ellipsis !important;
        }
    }

    @media screen and (max-width: 1280px) {
        [data-plugin-style-id="docker-elastic-plugin"] .node-header .hostname, [data-plugin-style-id="docker-elastic-plugin"] .node-header .node-id {
            max-width: 100px;
            min-width: 50px;
            overflow: hidden !important;
            text-overflow: ellipsis !important;
        }
    }

    [data-plugin-style-id="docker-elastic-plugin"] dl.properties.inline dt:first-child {
        min-width: auto;
        margin-left: 10px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dt:after {
        content: ":";
        padding: 0px 5px 0px 2px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] dd {
        float: left;
        padding: 5px 0px;
        font-weight: 600;
        margin: 0;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .docker .header {
        margin: 20px 0px 0px 20px;
    }

    [data-plugin-style-id="docker-elastic-plugin"] .warning {
        font-size: 13px;
        font-weight: 600;
        margin-bottom: 10px;
        border-left: 2px solid #f7bc08;
        padding: 10px;
        background: #fff;
        border-radius: 2px;
    }
</style>

<div data-plugin-style-id="docker-elastic-plugin">
    <div class="page_header">
        <h1 class="page_title">Plugin Status Report</h1>
    </div>
    <div class="docker">
        <div class="node-content">
            <div>
                <dl class="properties">
                    <dt>CPU</dt>
                    <dd>${cpus!}</dd>
                    <dt>Memory</dt>
                    <dd>${memory!}</dd>
                </dl>

                <dl class="properties">
                    <dt>OS</dt>
                    <dd>${os!}</dd>
                    <dt>Architecture</dt>
                    <dd>${architecture!}</dd>
                </dl>

                <dl class="properties">
                    <dt>Docker version</dt>
                    <dd>${dockerVersion!}</dd>
                </dl>
            </div>
            <h4 class="header">Running containers</h4>
            <table>
                <thead>
                <tr>
                    <th>Id</th>
                    <th>Job Identifier</th>
                    <th>Image</th>
                    <th>State</th>
                    <th>Created At</th>
                </thead>
                </tr>
                <tbody>
                            <#if containerStatusReports?size != 0>
                                <#list containerStatusReports as containerStatus>
                                <tr>
                                    <td>${containerStatus.id!}</td>
                                    <td>
									<#if containerStatus.jobIdentifier??>
                                        <a rel="nofollow noreferrer" target="_blank"
                                           href="/go/admin/status_reports/cd.go.contrib.elastic-agent.docker/${containerStatus.elasticAgentId!}">${containerStatus.jobIdentifier.representation!}</a>
                                    </#if>
                                    </td>
                                    <td>${containerStatus.image!}</td>
                                    <td>${containerStatus.state!}</td>
                                    <td>{{ ${containerStatus.createdAt?long?c!} | date:"MMM dd, yyyy hh:mm:ss a"}}</td>
                                </tr>
                                </#list>
                            <#else>
                            <tr>
                                <td colspan="5" class="message">No running containers.</td>
                            </tr>
                            </#if>
                </tbody>
            </table>
        </div>
    </div>
</div>
