# ARM Templates and Deploying Cellbase to Azure

This document contains information related to the deployment of Cellbase to Azure using ARM automation scripts.

## Deploy to Azure

### With the Portal

Click the following link the ensure you fill in the parameters according to their descriptions.

<a href="https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fraw.githubusercontent.com%2Fmarrobi%2Fcellbase%2Ffeature-arm-templates%2Fcellbase-app%2Fapp%2Fazure%2Farm%2Fazuredeploy.json" target="_blank">
    <img src="http://azuredeploy.net/deploybutton.png"/>
</a>

### With `az cli`

1. Clone the repository and move into the `ARM` directory with `cd ./cellbase-app/app/azure/arm`.
2. Using your editor fill in the `azuredeploy.parameters.json` with the required parameters
   > Note: `_artifactsLocation` should be set to the correct `raw.github.com` address for the branch you want to deploy. For example, use `https://raw.githubusercontent.com/marrobi/cellbase/feature-arm-template/cellbase-app/app/azure/arm/` .
3. Run `az deployment create --location northeurope --template-file azuredeploy.json --parameters @azuredeploy.parameters.json --name MyDeploymentNameHere --parameters`

## Deployment Sizing

The ARM templates defined here support two "t-shirt-sized" deployments. Each of these sizes defines properties such as the number of HDInsight master nodes, the size of VMs, the types of disks those VMs use etc. While it's possible to tweak each of these properties independently, these t-shirt sizes should give you some decent defaults.

The sizes are:

- Small (1): Useful for small teams, or individuals.
- Medium (2): A decent default for most installs that need so support a team of researchers


Here are the properties that are defined for each t-shirt size:

| Component   | Property            | 1 (Small)        | 2 (Medium) |
| ----------- | ------------------- | ------------ | ------ | 
| MongoDB     |
|             | node-quanity        | 1            | 3      |
|             | node-size           | D4s_v3         | E8s_v3   |
|             | disk-type           | E10          | P20    | 
|             |                     |              |
| Web Servers |
|             | node-quantity       | 1            | 2      | 
|             | node-size           | D2s_v3        | D4s_v3  | 
|             | disk-type           | HDD          | HDD    | 