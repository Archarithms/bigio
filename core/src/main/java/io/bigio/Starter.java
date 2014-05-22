/*
 * Copyright (c) 2014, Archarithms Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the FreeBSD Project.
 */

package io.bigio;

import io.bigio.core.ClusterService;
import io.bigio.core.Container;
import io.bigio.core.ListenerRegistry;
import io.bigio.core.MCDiscovery;
import io.bigio.core.member.MemberHolder;
import java.lang.management.ManagementFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the main entry point for BigIO.
 * 
 * @author Andy Trimble
 */
public class Starter {

    private static final boolean MONITOR_THREAD_CONTENTION = true;

    private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

    public Starter() {
        if(MONITOR_THREAD_CONTENTION) {
            ManagementFactory.getThreadMXBean().setThreadContentionMonitoringEnabled(MONITOR_THREAD_CONTENTION);
        }

//        Starter.bootstrap();
    }

    /**
     * Bootstrap the system and return a Speaker object.
     * 
     * @return an initialized speaker object.
     */
    public static Speaker bootstrap() {
        Speaker speaker = new Speaker();
        ClusterService cluster = new ClusterService();
        MemberHolder memberHolder = new MemberHolder();
        ListenerRegistry registry = new ListenerRegistry();
        MCDiscovery mc = new MCDiscovery();
        mc.setMemberHolder(memberHolder);
        cluster.setMulticastDiscovery(mc);
        cluster.setMemberHolder(memberHolder);
        cluster.setRegistry(registry);
        speaker.setCluster(cluster);
        speaker.init();
        return speaker;
    }

    public static void exit() {
        LOG.info("Goodbye");
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        Parameters.INSTANCE.currentOS(); // Just to load the properties
        Container.INSTANCE.scan();
        Starter starter = new Starter();
    }
}
