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
Here below the instruction how to deploy into DigitalOcean with help of `docker-machine`. It should be easy (again due to various cloud drivers supported by docker machine) to apply the same steps for any cloud provider.

1. Get DigitalOcean access token from https://cloud.digitalocean.com/settings/applications
2. Run `provision/docker-machine-provision <your digitalocean access token>` - it will create droplet with Debian 8.2 x64 image with 512mb of RAM, in AMS3 datacenter, named `marketing` (feel free to modify `docker-machine-provision` to meet your needs)
3. Modify your docker env by running `eval "$(docker-machine env marketing)"`, it will export `DOCKER_HOST` and `DOCKER_CERT_PATH` among others which make your `docker` and `docker-compose` commands to operate on DigitalOcean image instead of local host
4. Run `export MARKETING_REPO_URL=<your git repository SSH url> && docker-compose -d up`, to build and start container inside DigitalOcean in detached mode (`-d` flag) (`MARKETING_REPO_URL` is the only required parameter for the application to start, so it will fail if it is not provided)
5. Final step, as the application in order to clone, as well as push into your repository, SSH key must configured, to do so:

    5.1. Ssh to your cloud image with `docker-machine ssh marketing`
    5.2. Run `ssh-keygen` (docker container has a volume mapping between `/root/.ssh`)
    5.3. Add generated public key as additional SSH key into your Git repository to allow access using generate private key

*Some steps could be automated, lets say with Ansible, for now I don't feel like it is necessary to do.*

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
