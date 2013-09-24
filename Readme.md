

Holico - Home Life Context
==========================

Holico is a test implementation of an "Home Bus". It provides a communications bus across many devices in your Home Network (LAN).

Presentation
------------

The goal of the Holico bus is to allow interoperability between heterogeneous devices in a home. It can be used to make your tablet interact with your fridge, your internet gateway and your heating system, for example.

The Holico bus gives access to a shared data structure containing three main kind of objects : 

  * **Nodes** : a Node represents a stakeholder on the bus, that is a device that use holico to communicate with other devices. A Node exposes the topics it can publish to and the services (behaviors) it can provide.
  
  * **Topics** : communication on the bus is based on the publish / subscribe paradigm : a Node can publish a state on a Topic to inform any other interested Node that a change has occurred in the current home state. 
  For example your alarm clock may publish the "waking time" state in the "activity" topic and all devices that have subscribed to this topic can take appropriate action (your coffee machine will probably start making coffee, etc.).
  
  * **Rules** : a rule binds a topic to a service provided by a Node. It's a very simple mechanism to allow configuration a scenario-like behaviours by the user. 
  In the previous example, the "prepare coffee" service of the coffee machine is bound to the "activity" topic with the following rule : 
  
    " **If** 'topic.activity' == 'waking time' **Then** use the service coffeeMachine.prepareCoffee"
  

Building
--------

Holico is built with maven :

  * checkout the current repository
  * in the root folder, to build all libs and application except android applications use : 
   mvn clean install
  * if you also want to build the android test applications, you can use the `with-android` maven profile :
   mvn install -Pwith-android
  * tests include network acccess and might be very long, you may need to skip them :
   -Dmaven.test.skip=true   

To build the android sample applications, you also need to install the android support library to your 
local maven repository : 

mvn install:install-file -Dfile=<path-to-file> -DgroupId=com.google.android -DartifactId=support-v4 -Dversion=r13 -Dpackaging=jar

<path-to-file> will be something like $ANDROID_HOME/extras/android/support/v13/android-support-v13.jar


Running the samples
-------------------

This repository contains a (beta !) implementation of the holico stack and several samples that can used to test and experiment the principles behind the "home bus" idea.

  * **holico-node-simulator-cli** : a command-line driven sample node
  * **holico-node-simulator-pc** : a gui-based sample node
  * **holico-node-simulator-android** : a sample node running on an android device
  * **holico-dashboard-pc** : a pc application that displays the rules currently configured on your home bus
  
  
Limitations 
-----------

  * This implementation only currently requires java 1.6 (and above).
  * This is a test and demo implementation : expect bugs and shortcomings
  * The way we used coap for this implementation is not perfect, it will be improved in the next versions.

  
License
-------

Copyright (C) 2013 Orange

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Orange nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


