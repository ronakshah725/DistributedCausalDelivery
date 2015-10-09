Multicast:
1. unzip folder in home directory of network machines
2. goto multicast folder which will be created
3. Open 11 terminals ssh into 11 dc** machines and cd into the multicast folder (11 since in my system, there is a controller node for establishing socket connections and terminating them)
4. In any one terminal compile all files type :make
5. In any one terminal start controller type :make controller
6. in other terminal type :make n1 //starts node with id 1
7. repeat step 6 on 9 other terminals typing make n2, make n3, make n4....
8. Analysis files are generated in the multicast folder named 01analysis.txt, 02analysis.txt etc. 


broadcast:
1. Goto broadcast folder
2. make sure you are in broadcast folder in terminal
3. Open 11 terminals ssh into 11 dc** machines and cd into the broadcast folder (11 since in my system, there is a controller node for establishing socket connections and terminating them)
4. In any one terminal compile all files type :make
5. In any one terminal start controller type :make controller
6. in other terminal type :make n1 //starts node with id 1
7. repeat step 6 on 9 other terminals typing make n2, make n3, make n4....
8. Analysis files are generated in the broadcast folder named 01analysis.txt, 02analysis.txt etc. 