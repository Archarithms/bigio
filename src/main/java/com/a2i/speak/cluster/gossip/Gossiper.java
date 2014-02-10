///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.a2i.speak.cluster.gossip;
//
//import com.a2i.speak.cluster.Member;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicBoolean;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// *
// * @author atrimble
// */
//public class Gossiper {
//
//    private static final Logger LOG = LoggerFactory.getLogger(Gossiper.class);
//
//    private String multicast = "";
//
//    private int gossipInterval; //in ms
//    
//    private int cleanupInterval; //in ms
//
//    private Random random;
//
//    private String myAddress;
//
//    private Member me = null;
//
//    public Gossiper(String bindAddress) {
//
//        gossipInterval = 100;
//
//        cleanupInterval = 10000;
//
//        random = new Random();
//
//        this.myAddress = bindAddress + ":" + commandPort;
//
//        List<Member> startupMemberList = parseInitialMembers();
//
//        for (Member member : startupMemberList) {
//            if (member.getIp().contains(bindAddress)) {
//                me = member;
//            }
//        }
//
//        if(me == null) {
//            me = new Member();
//        }
//
//        if(LOG.isDebugEnabled()) {
//            for (Member member : startupMemberList) {
//                LOG.debug(member.toString());
//            }
//        }
//    }
//
//    private List<Member> parseInitialMembers() {
//        ArrayList<String> startupHostsList = new ArrayList<String>();
//        File startupConfig = new File("config", "startup_members");
//
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(startupConfig));
//            String line;
//            while ((line = br.readLine()) != null) {
//                startupHostsList.add(line.trim());
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return startupHostsList;
//    }
//
//    private void sendMembershipList() {
//
//        this.me.setHeartbeat(me.getHeartbeat() + 1);
//
//        synchronized (this.memberList) {
//            try {
//                Member member = getRandomMember();
//
//                if (member != null) {
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    ObjectOutputStream oos = new ObjectOutputStream(baos);
//                    oos.writeObject(this.memberList);
//                    byte[] buf = baos.toByteArray();
//
//                    String address = member.getAddress();
//                    String host = address.split(":")[0];
//                    int port = Integer.parseInt(address.split(":")[1]);
//
//                    InetAddress dest;
//                    dest = InetAddress.getByName(host);
//
//                    System.out.println("Sending to " + dest);
//                    System.out.println("---------------------");
//                    for (Member m : memberList) {
//                        System.out.println(m);
//                    }
//                    System.out.println("---------------------");
//
//                    //simulate some packet loss ~25%
//                    int percentToSend = random.nextInt(100);
//                    if (percentToSend > 25) {
//                        DatagramSocket socket = new DatagramSocket();
//                        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, dest, port);
//                        socket.send(datagramPacket);
//                        socket.close();
//                    }
//                }
//
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//        }
//    }
//
//    private Member getRandomMember() {
//        Member member = null;
//
//        if (this.memberList.size() > 1) {
//            int tries = 10;
//            do {
//                int randomNeighborIndex = random.nextInt(this.memberList.size());
//                member = this.memberList.get(randomNeighborIndex);
//                if (--tries <= 0) {
//                    member = null;
//                    break;
//                }
//            } while (member.getAddress().equals(this.myAddress));
//        } else {
//            System.out.println("I am alone in this world.");
//        }
//
//        return member;
//    }
//
//    private class MembershipGossiper implements Runnable {
//
//        private AtomicBoolean keepRunning;
//
//        public MembershipGossiper() {
//            this.keepRunning = new AtomicBoolean(true);
//        }
//
//        @Override
//        public void run() {
//            while (this.keepRunning.get()) {
//                try {
//                    TimeUnit.MILLISECONDS.sleep(gossipInterval);
//                    sendMembershipList();
//                } catch (InterruptedException e) {
//                                        // TODO: handle exception
//                    // This membership thread was interrupted externally, shutdown
//                    e.printStackTrace();
//                    keepRunning.set(false);
//                }
//            }
//
//            this.keepRunning = null;
//        }
//
//    }
//
//    private class AsychronousReceiver implements Runnable {
//
//        private AtomicBoolean keepRunning;
//
//        public AsychronousReceiver() {
//            keepRunning = new AtomicBoolean(true);
//        }
//
//        @SuppressWarnings("unchecked")
//        @Override
//        public void run() {
//            while (keepRunning.get()) {
//                try {
//                    //XXX: be mindful of this array size for later
//                    byte[] buf = new byte[256];
//                    DatagramPacket p = new DatagramPacket(buf, buf.length);
//                    server.receive(p);
//
//                                        // extract the member arraylist out of the packet
//                    // TODO: maybe abstract this out to pass just the bytes needed
//                    ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
//                    ObjectInputStream ois = new ObjectInputStream(bais);
//
//                    Object readObject = ois.readObject();
//                    if (readObject instanceof ArrayList<?>) {
//                        ArrayList<Member> list = (ArrayList<Member>) readObject;
//
//                        System.out.println("Received member list:");
//                        for (Member member : list) {
//                            System.out.println(member);
//                        }
//                        // Merge our list with the one we just received
//                        mergeLists(list);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    keepRunning.set(false);
//                } catch (ClassNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        private void mergeLists(ArrayList<Member> remoteList) {
//
//            synchronized (Client.this.deadList) {
//
//                synchronized (Client.this.memberList) {
//
//                    for (Member remoteMember : remoteList) {
//                        if (Client.this.memberList.contains(remoteMember)) {
//                            Member localMember = Client.this.memberList.get(Client.this.memberList.indexOf(remoteMember));
//
//                            if (remoteMember.getHeartbeat() > localMember.getHeartbeat()) {
//                                // update local list with latest heartbeat
//                                localMember.setHeartbeat(remoteMember.getHeartbeat());
//                                // and reset the timeout of that member
//                                localMember.resetTimeoutTimer();
//                            }
//                        } else {
//                                                        // the local list does not contain the remote member
//
//                                                        // the remote member is either brand new, or a previously declared dead member
//                            // if its dead, check the heartbeat because it may have come back from the dead
//                            if (Client.this.deadList.contains(remoteMember)) {
//                                Member localDeadMember = Client.this.deadList.get(Client.this.deadList.indexOf(remoteMember));
//                                if (remoteMember.getHeartbeat() > localDeadMember.getHeartbeat()) {
//                                    // it's baa-aack
//                                    Client.this.deadList.remove(localDeadMember);
//                                    Member newLocalMember = new Member(remoteMember.getAddress(), remoteMember.getHeartbeat(), Client.this, t_cleanup);
//                                    Client.this.memberList.add(newLocalMember);
//                                    newLocalMember.startTimeoutTimer();
//                                } // else ignore
//                            } else {
//                                // brand spanking new member - welcome
//                                Member newLocalMember = new Member(remoteMember.getAddress(), remoteMember.getHeartbeat(), Client.this, t_cleanup);
//                                Client.this.memberList.add(newLocalMember);
//                                newLocalMember.startTimeoutTimer();
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    private void start() throws InterruptedException {
//
//        // Start all timers except for me
//        for (Member member : memberList) {
//            if (member != me) {
//                member.startTimeoutTimer();
//            }
//        }
//
//        // Start the two worker threads
//        ExecutorService executor = Executors.newCachedThreadPool();
//                //  The receiver thread is a passive player that handles
//        //  merging incoming membership lists from other neighbors.
//        executor.execute(new AsychronousReceiver());
//                //  The gossiper thread is an active player that 
//        //  selects a neighbor to share its membership list
//        executor.execute(new MembershipGossiper());
//
//                // Potentially, you could kick off more threads here
//        //  that could perform additional data synching
//        // keep the main thread around
//        while (true) {
//            TimeUnit.SECONDS.sleep(10);
//        }
//    }
//
//    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
//
//        Client client = new Client();
//        client.start();
//    }
//
//    @Override
//    public void handleNotification(Notification notification, Object handback) {
//
//        Member deadMember = (Member) notification.getUserData();
//
//        System.out.println("Dead member detected: " + deadMember);
//
//        synchronized (this.memberList) {
//            this.memberList.remove(deadMember);
//        }
//
//        synchronized (this.deadList) {
//            this.deadList.add(deadMember);
//        }
//    }
//}
