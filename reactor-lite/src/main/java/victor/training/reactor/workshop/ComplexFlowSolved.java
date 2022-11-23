//package victor.training.reactive.reactor.lite;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.With;
//import reactor.core.publisher.Mono;
//import victor.training.reactive.reactor.workshop.Enrich.A;
//import victor.training.reactive.reactor.workshop.Enrich.B;
//import victor.training.reactive.reactor.workshop.Enrich.C;
//
//import java.util.concurrent.CompletableFuture;
//
//public class ComplexFlowSolved extends ComplexFlow {
//
//  public ComplexFlowSolved(Dependency dependency) {
//    super(dependency);
//  }
//
//  public Mono<Void> p06_complexFlow(int id) {
//    //        CompletableFuture<A> fa = dependency.a(id);
//    //        CompletableFuture<B> fb = fa.thenCompose(dependency::b1);
//    //        CompletableFuture<C> fc = fa.thenCompose(dependency::c1);
//    //        CompletableFuture<A> fa1 = allOf(fa, fb, fc).thenApply(v -> logic(fa.join(), fb.join(), fc.join()));
//    //        fa1.thenAccept(a1 -> dependency.auditA(a1, fa.join()));
//    //        return fa1.thenAccept(a1 -> dependency.saveA(a1));
//
//
//    return dependency.a(id)
//            .thenApply(a -> new MyContext().withA(a))
//
//            // *** sequential
//            // .thenCompose(context -> dependency.b1(context.a).thenApply(context::withB)
//            // .thenCompose(context -> dependency.c1(context.a).thenApply(context::withC))
//
//            // *** parallel
//            .thenCompose(context->
//                    dependency.b1(context.a).thenCombine(
//                            dependency.c1(context.a), (b,c)->context.withB(b).withC(c)))
//
//            .thenApply(context -> context.withA1(logic(context.a, context.b, context.c)))
//
//            .thenCompose(context -> dependency.saveA(context.a1).thenApply(a -> context))
//
//            .whenComplete((context, e__) -> {
//              if (context != null) dependency.auditA(context.a1, context.a)
//                      .whenComplete((v, err)-> {
//                        if (err != null) {
//                          err.printStackTrace();
//                        }
//                      });
//            })
//            .thenApply(context -> null);
//  }
//  @Data
//  @AllArgsConstructor
//  @With
//  private static class MyContext {
//    public final Enrich.A a;
//    public final Enrich.B b;
//    public final Enrich.C c;
//    public final Enrich.A a1;
//    public MyContext() {
//      this(null, null, null, null);
//    }
//  }
//}
