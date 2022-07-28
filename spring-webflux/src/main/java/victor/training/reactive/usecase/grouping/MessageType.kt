package victor.training.reactive.usecase.grouping

enum class MessageType {
    TYPE1_NEGATIVE, TYPE2_ODD, TYPE3_EVEN;

    companion object {
        fun forMessage(message: Int): MessageType {
            if (message < 0) return TYPE1_NEGATIVE
            return if (message % 2 == 1) TYPE2_ODD else TYPE3_EVEN
        }
    }
}