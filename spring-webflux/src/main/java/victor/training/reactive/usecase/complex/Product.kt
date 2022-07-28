package victor.training.reactive.usecase.complex

data class Product(
    var id: Long? = null,
    var name: String? = null,
    var isActive: Boolean = false,
    var isResealed: Boolean = false,
    var rating: ProductRatingResponse? = null,
)