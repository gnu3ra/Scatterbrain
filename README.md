# Scatterbrain
City-spanning BLE 'Lazy mesh' network chat. Hold internet-less long range conversations

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
retransmitted to any other peers met in the future. This app uses the BLEMingle library (https://github.com/GitGarage/BLEMingleDroid) for eventual compatability with iOS.

### Wow! Can I use it now?
Sadly, no. The protocol is still far from complete. The UI in the android app is a placeholder
right now while the backend is built more. (You can
discover people near you, send fake messages, but not much else). But you can feel
free to contribute!

