# Scatterbrain
City-spanning multi-protocol 'Lazy mesh' network chat. Hold internet-less long range conversations based on
the Wind concept (https://github.com/n8fr8/WindFarm).

### What does it do?
Scatterbrain is a distributed discussion board using bluetooth low energy, smartphones, and
eventually other consumer devices. It provides a framework for sending text based messages,
replying to an existing message, and maintaining an identity.

### Background
Mesh networks have been frequently used for communications for many different applications,
but they have never seen widespread consumer use. Here's why:

- Hard to use: p2p meshes need knowledge of advanced networking and tons of configuration
- Hard on battery / bandwidth: Most people pay for data, and no one wants a network hog.
- Not enough for mobile: Search 'mesh' on your app store and be disappointed.
- Needs lots of peers: I hate begging my friends to use netsukuku.

Scatterbrain aims to solve most or all of these problems. 

### Here's How
Simply put, Scatterbrain is a mesh network that does not need to be constantly connected. 
People can leave and enter small networks, and traffic follows them, online and offline. 
When two people pass on the street, Scatterbrain wirelessly exchanges messages, even ones 
sent by friends. The end result is a system where a single message can spread to an entire city, 
even if the mesh does not. When a message is sent, it is stored on the receiving device and
retransmitted to any other peers met in the future. This app can operate over most protocols (it uses bluetooth now) and is theoretically compatible with iOS.
Check out the protocol at https://github.com/gnu3ra/Scatterbrain-Protocol if you want to help out making services for the network.

### Wow! Can I use it now?
Sort of.
Grab an apk from https://dl.scatterbrain.xyz/senpaiDetector/

 Zero hop messaging works (you can text people directly near you, like a generic bluetooth chat), and service discovery works to an extent. It does not scale very well yet, as the datastore and  most of the scatterbrain protocol has yet to be implemented. 


### I wana help take over the world! How?
Find us on IRC: #scatterbrain on freenode!

There is also a telegram group at https://telegram.me/joinchat/DGewIz7dadgXHYp05sa3PQ for public beta testing and user support.

We need large scale testers. Grab some friends and try it out in public places like malls or raves or things.

You can donate some bitcoins if you feel like it: 15FdcxEJziSFE8Dt5MpiKmi5824LF7iH9Y . All proceeds go to ad blitzes or snacks for developers. 