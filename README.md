### About
Experiment using Git as a data store to collect users emails for startups 
during product development when not yet public beta is available. 

For serious data driven backend this approach is not suitable, but for small set of data it should work fine.

A few advantages or key features:
- Git repository can be integrated with Slack or Hipchat to receive notifications every time the user signs up
- Use Github or Bitbucket as the data storage, so no need to take care of data replication and data loss yourself

### Available environment variables
<table>
    <tr>
        <th>Name (default value)</th>
        <th>Purpose</th>
        <th>Possible values</th>
    </tr>
    <tr>
        <td>MARKETING_PORT (3000)</td>
        <td>Port on which the application is running</td>
        <td>Any available port. As application is supposed to be run in Docker container, default value is a good one.</td>
    </tr>
    <tr>
        <td>MARKETING_HOST (localhost)</td>
        <td>Host Jetty binds to</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>MARKETING_REPO_URL</td>
        <td>*Required*. Repository URL to be used to store emails.txt. Will clone the repository, and push changes there.</td>
        <td>Existing URL on Github or Bitbucket using SSH protocol.</td>
    </tr>
    <tr>
        <td>MARKETING_WEB_FLUSH_COMMAND (flush)</td>
        <td>REST endpoint to flush the data manually into emails.txt</td>
        <td>Any REST endpoint of your choice without slash</td>
    </tr>
    <tr>
        <td>MARKETING_WEB_FLUSH_COMMAND_ENABLED (1)</td>
        <td>Whether flush command defined above is enabled</td>
        <td>1 | 0. 0 - to disable flush the data in memory manually from REST</td>
    </tr>
    <tr>
        <td>MARKETING_FLUSH_INTERVAL_MINS (10)</td>
        <td>How often flush the data in memory into file</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>MARKETING_FLUSH_THRESHOLD (10000)</td>
        <td>Number of entries/emails in memory before flush into the file</td>
        <td>&nbsp;</td>
    </tr>
    <tr>
        <td>MARKETING_FLUSH_DRY_RUN (0)</td>
        <td>Whether push the changes into remote repository or skip this part and just commit locally. Good for testing and developing locally. </td>
        <td>1 | 0. 1 - to enable dry run, so no push will be performed. REPO_URL still must be valid as it will do clone first.</td>
    </tr>
</table>

### Deployment
Docker part is finalized now. Details are coming soon. Check Dockerfile, docker-compose.yml and provision/ directory for now.

### License
The MIT License (MIT)

Copyright (c) 2015 Aliaksandr Kazlou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
