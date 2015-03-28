# DroidMessenger
Instant Messaging App that uses Parse and Pubnub

Whats up everyone!

I wanted to learn how to take advantage of real time events using Parse so here is a little project I put together. A Parse and Pubnub account will be needed for this. If you are unfamiliar with the two services here are the respective URL's.

Pubnub: http://pubnub.com
Parse: http://parse.com


Once these accounts are setup you need to create an application on both platforms and place the required information in the project string resources xml file. Here is an example of what is will look like.

    <!-- Parse -->
    <string name="parse_application_id">Your Parse application ID</string>
    <string name="parse_client_id">Your Parse client ID</string>

    <!-- PubNub -->
    <string name="pubnub_publish_key">Your Pubnub Publish Key</string>
    <string name="pubnub_subscribe_key">Your Pubnub Subscribe Key</string>


Once this information has been imported you need to add a class with the name "Message" to your Parse database with the following columns.

COLUMN-NAME               OBJECT-TYPE
messageBody               String
timeSent                  Date
senderId                  String
receiverId                String


After this is complete and you have ensured the proper keys are entered. The application should build.

Sign up with a few mock users and test away.

If you run into any issues or have any suggestions don't hesitate!



