package victor.training.reactive.usecase.complex

data class Product(
    val id: Long, // long
    val name: String,
    val isActive: Boolean,
    val isResealed: Boolean,
    var rating: ProductRatingResponse? = null,
)