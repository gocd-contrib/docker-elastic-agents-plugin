<style>
    [data-plugin-style-id="docker-elastic-agent"] .outer-container {
        display: table;
        position: absolute;
        height: calc(100vh - 119px);
        width: 100%;
    }

    [data-plugin-style-id="docker-elastic-agent"] .container {
        display: table-cell;
        vertical-align: middle;
    }

    [data-plugin-style-id="docker-elastic-agent"] .error-container {
        border: 1px solid #eee;
        margin-top: 15px;
        border-radius: 3px;
    }

    [data-plugin-style-id="docker-elastic-agent"] .message {
        color: #991A00;
        padding: 1rem;
        font-size: 15px;
        background: #f6f6f6;
        font-weight: 600;
        vertical-align: middle;
    }

    [data-plugin-style-id="docker-elastic-agent"] .stacktrace {
        height: calc(100vh - 210px);
    }

    [data-plugin-style-id="docker-elastic-agent"] textarea[readonly="readonly"], textarea[readonly] {
        cursor: auto;
        margin-bottom: 0px;
    }

    [data-plugin-style-id="docker-elastic-agent"] .logs {
        color: white;
        display: block;
        height: calc(100vh - 210px);
        font-size: 13px;
        max-height: 100%;
        font-weight: 400;
        font-family: monaco, sans-serif;
        padding-left: 10px;
        background-color: #383838;
    }
</style>

<div data-plugin-style-id="docker-elastic-agent">
    <div class="outer-container">
        <div class="container">
            <div class="error-container">
                <div class="message">
                    Error while generating status report: ${ message! }
                </div>
                <div class="stacktrace">
                    <textarea class="logs" readonly>${stacktrace!}</textarea>
                </div>
            </div>
        </div>
    </div>
</div>
