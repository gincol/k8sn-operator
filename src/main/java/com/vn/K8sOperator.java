//package com.vn;
//
//import javax.inject.Inject;
//
//import io.javaoperatorsdk.operator.Operator;
//import io.quarkus.runtime.Quarkus;
//import io.quarkus.runtime.QuarkusApplication;
//import io.quarkus.runtime.annotations.QuarkusMain;
//
//@QuarkusMain
//public class K8sOperator implements QuarkusApplication {
//	
//	@Inject
//	Operator operator;
//
//	public static void main(String... args) {
//		Quarkus.run(K8sOperator.class, args);
//	}
//
//	@Override
//	public int run(String... args) throws Exception {
//		operator.start();
//		Quarkus.waitForExit();
//		return 0;
//	}
//	
//}
