package win.sinno.dispatch.core;

import org.junit.Test;
import win.sinno.dispatch.core.agent.MachineAgent;
import win.sinno.dispatch.model.Machine;

import java.util.Set;

/**
 * 主 守护进程 est
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 上午11:58
 */
public class MainDaemonTest {

    @Test
    public void testInitClient() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.startCuratorClient();
    }

    @Test
    public void testS1() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.start();

    }

    @Test
    public void testS2() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.start();

        try {

            Thread.sleep(20000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //machine agent
        MachineAgent machineAgent = mainDaemon.getMachineAgent();
        Machine leaderMachine = machineAgent.getLeaderMachine();

        Set<Machine> workerMachines = machineAgent.getWorkerMachines();

        System.out.println("leader machine:" + leaderMachine);

        System.out.println("-----");

        System.out.println("worker machine :");

        for (Machine machine : workerMachines) {
            System.out.println(machine.toString());
        }

        System.out.println(machineAgent.status());

    }

    @Test
    public void testS3() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.start();

        try {

            Thread.sleep(110000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //machine agent
        MachineAgent machineAgent = mainDaemon.getMachineAgent();
        Machine leaderMachine = machineAgent.getLeaderMachine();

        Set<Machine> workerMachines = machineAgent.getWorkerMachines();

        System.out.println("leader machine:" + leaderMachine);

        System.out.println("-----");

        System.out.println("worker machine :");

        for (Machine machine : workerMachines) {
            System.out.println(machine.toString());
        }

        System.out.println(machineAgent.status());
    }

    @Test
    public void testS4() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.start();

        try {

            Thread.sleep(70000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //machine agent
        MachineAgent machineAgent = mainDaemon.getMachineAgent();
        Machine leaderMachine = machineAgent.getLeaderMachine();

        Set<Machine> workerMachines = machineAgent.getWorkerMachines();

        System.out.println("leader machine:" + leaderMachine);

        System.out.println("-----");

        System.out.println("worker machine :");

        for (Machine machine : workerMachines) {
            System.out.println(machine.toString());
        }

        System.out.println(machineAgent.status());
    }
}
