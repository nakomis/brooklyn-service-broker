
# Using the Broker

## Registering the Broker and Servcies

First, register the service broker

    $ cf create-service-broker <broker-name> <user> <password> <url>
    
Pass the security user and password you set for the broker, and the URL where the broker is running.

You'll now need to enable access to services.
Check for any new services that have no access:

    $ cf service-access 
    
Enable those services that you wish to appear in the marketplace:

    $ cf enable-service-access <service-name>

Check that they are available:

    $ cf marketplace


## Creating Services

To create a service and bind it to an application:

    $ cf create-service <service-name> <plan-name> <service-instance-id>
    $ cf bind-service my-app my-service
    
To unbind and delete a service:

    $ cf unbind-service my-app my-service
    $ cf delete-service <service-instance-id>


## Adding New Services

You can add new services to the Brooklyn catalog in the Brooklyn GUI.
To make these available in the Cloud Foundry marketplace, you'll need to:

    $ cf update-service-broker <broker-name> <user> <password> <url>

And then repeat the `enable-service-access` command above.
