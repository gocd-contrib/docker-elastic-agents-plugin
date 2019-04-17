<style>
	.main-container {
		margin-top: 0;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .container {
		background: transparent !important;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .page_header {
		display:         flex;
		width:           100%;
		justify-content: space-between;
		align-items:     center;
		background:      #fff;
		box-shadow:      0 0 0 #000;
		height:          50px;
		box-sizing:      border-box;
		border-bottom:   1px solid #ccc;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .page_title {
		font-size:   17px;
		font-weight: 600;
		margin:      0;
		align-self:  center;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker {
		border:        1px solid #d1c4e9;
		padding:       25px;
		margin-top:    65px;
		background:    #fff;
		border-radius: 3px;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker table {
		width:     100%;
		border:    1px solid #D8D8D8;
		margin:    10px 0px 0px 0px;
		font-size: 13px;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker table thead {
		background: #b9f6ca;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker table thead th {
		padding:     10px 20px;
		font-weight: bold;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker table tbody tr {
		border-top:      1px solid #D8D8D8;
		border-bottom:   1px solid #D8D8D8;
		border-collapse: collapse;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker table tbody td {
		padding:       10px 20px;
		max-width:     190px;
		text-overflow: ellipsis;
		overflow:      hidden;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .message {
		font-size:   13px;
		font-weight: 600;
	}

	[data-plugin-style-id="docker-elastic-plugin"] dl.properties {
		overflow:       hidden;
		margin:         0;
		font-size:      13px;
		display:        inline-block;
		vertical-align: top;
	}

	[data-plugin-style-id="docker-elastic-plugin"] dt {
		float:       left;
		clear:       both;
		margin:      0;
		padding:     5px 0px;
		font-weight: 600;
	}

	[data-plugin-style-id="docker-elastic-plugin"] dt:after {
		content: ":";
		padding: 0px 5px 0px 2px;
	}

	[data-plugin-style-id="docker-elastic-plugin"] dd {
		float:        left;
		padding:      5px 0px;
		margin-right: 50px;
	}

	[data-plugin-style-id="docker-elastic-plugin"] .docker .header {
		margin: 20px 0px 0px 0px;
	}
</style>

<div data-plugin-style-id="docker-elastic-plugin">
	<div class="container">
		<div class="page_header">
			<h1 class="page_title">Cluster Status Report</h1>
		</div>
		<div class="docker row">
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
								   href="/go/admin/status_reports/cd.go.contrib.elastic-agent.docker/agent/${containerStatus.elasticAgentId!}?job_id=${containerStatus.jobIdentifier.jobId}">${containerStatus.jobIdentifier.representation!}</a>
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
