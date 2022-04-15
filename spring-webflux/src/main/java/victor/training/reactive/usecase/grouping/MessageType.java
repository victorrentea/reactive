package victor.training.reactive.usecase.grouping;

enum MessageType {
   TYPE1_NEGATIVE,
   TYPE2_ODD,
   TYPE3_EVEN;

   public static MessageType forMessage(Integer message) {
      if (message < 0) return TYPE1_NEGATIVE;
      if (message % 2 == 1) return TYPE2_ODD;
      else return TYPE3_EVEN;
   }
}
