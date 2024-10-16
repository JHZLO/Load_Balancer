package org.loadBalancer;

import org.loadBalancer.controller.LoadBalancerController;

public class Main {
    public static void main(String[] args){
        new LoadBalancerController().run();
    }
}
