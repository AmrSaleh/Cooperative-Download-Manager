# Cooperative-Download-Manager
## Cooperative downloading over multiple clients
We introduced a client program allowing users to cooperatively download pieces of large files and then combine them over local network.

## Team members by alphabetical order
+ Ahmed Abdelwahed Elkhatib
+ Abdelrahman Ali Morgan
+ Amr Saleh Mohamed Aly

## Motivation
Usually we want to cooperatively download large files that we will use 
instead of a single person downloading it and suffering from the slow 
internet connection. If we could split the large files and download these 
chunks simultaneously from different devices, then we would download 
the file much faster and then we could share it together using local network, 
which is much faster than the broadband connection. 

## Project Overview
The solution we propose aims to overcome the problem of slow broadband 
connections and the fact that people in the same community circle, like 
friends and colleagues, usually would like to download the same files. So, 
instead of a single person downloading a massive file and suffering through 
his connection and wasting a lot of time, a group of friends can 
cooperatively download the needed file as chunks simultaneously; saving a 
whole lot of time. 
Our program manages the coordination and the file splitting into chunks, 
and most of all, keeping all the clients synchronized. It also facilitates 
merging the file at the end. 

## System Architecture
Our proposed system consists of 
+ **Downloader application** 
present on clients' machines. 
Each client coordinates through the dedicated server with other clients; 
deciding which parts of the desired file to download. 
+ **A web server coordinator** 
This server holds the information that allows each client to dynamically 
request and download available parts of files that haven't been 
downloaded yet. 
This mainly helps keep the coordination process smooth and dynamic 
between clients.

### Use Case
The first client uses the application to download a file from a server. The 
application collects the required information about the file (e.g. the file size, 
address, etc.) then sends this information to the coordinator server. The 
coordinator server divides the file into parts and assigns some parts to the 
first client to download. Other clients use the application to contact the 
coordinator server and request to contribute to downloading the file. The 
coordinator server assigns some parts of the file to the newly joined clients 
for downloading. When a client finishes downloading a part, it requests 
another one from the server. The server replies with a part that is not 
downloaded by any client. At the end, all file parts are downloaded once 
among all the contributed clients. 
Finally, those clients join a local network, which is supposedly faster than 
broadband, and use the application to merge all scattered parts of the file 
into one whole copy at each machine. 

## Diagram
![alt text](https://user-images.githubusercontent.com/5616594/27265489-3634e2d8-5497-11e7-8d6c-19d5f63fb344.PNG "Diagram")

## Flowchart
![alt text](https://user-images.githubusercontent.com/5616594/27265602-92e83b96-5498-11e7-926c-bdf5455b2d68.PNG "Flowchart")

## Assumptions
+ Files can be requested as chunks from server. 
+ Clients will join a local network to merge the file. 
+ We used a UDP broadcast method to exchange the packets over the 
local network. Unfortunately UDP packets have limited packet size and 
this caused the file packets to be small which may cause somewhat an 
overhead. 

## Results
+ We successfully managed to download chunks of a file in parallel. 
+ Also, we managed to merge those chunks and recreate the original file. 
+ We created a coordinator server with the responsibility of organizing the 
clients when issuing a new download request and making a new 
download session for it. 
+ Clients then can register themselves as participants in the session and 
request parts to download, periodically. 
+ The coordinator successfully communicated with the clients and 
responded with the chunks to be downloaded by each client. 
+ When all the chunks are downloaded and confirmed to the coordinator 
he then informs the clients that the download is finished and that they 
can join a local network to merge their files. 
+ We managed to make clients communicate locally and discover each 
other. 
+ Then they decide automatically on which packets to be broadcasted 
based on the control packets exchanged between them. 
+ We successfully tested the local client discovery and file chunks 
exchange process then we merged the resulting chunks and produced a 
working copy of the originally downloaded file at each client. 

## Video demonstration
Commentary is in Arabic but I plan to add English subtitles.

<a href="http://www.youtube.com/watch?feature=player_embedded&v=3cyurbn01gM
" target="_blank"><img src="http://img.youtube.com/vi/3cyurbn01gM/0.jpg" 
alt="Cooperative Download Manager - merging file parts over local network" width="240" height="180" border="10" /></a>

<a href="http://www.youtube.com/watch?feature=player_embedded&v=mjebX_kyQtU
" target="_blank"><img src="http://img.youtube.com/vi/mjebX_kyQtU/0.jpg" 
alt="Cooperative Download Manager - downloading file as chunks" width="240" height="180" border="10" /></a>

## References
+ [Parallel Multi-Threaded Downloader By LUU GIA THUY](https://github.com/luugiathuy/Java-Download-Manager)
+ [Network discovery using UDP Broadcast By Michiel Demey](http://michieldemey.be/blog/network-discovery-using-udp-broadcast/)
+ [Apache Common IO Library](https://commons.apache.org/proper/commons-io/)
