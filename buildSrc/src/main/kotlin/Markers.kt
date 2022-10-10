import kotlin.reflect.KMutableProperty0

const val PREFIX_RELEACE = "//!!REPLACE "
const val REMOVE = "//!!REMOVE"
const val REMOVE_START = "//!!REMOVE_START"
const val REMOVE_END = "//!!REMOVE_END"

private data class DeleteMarkBox(var isInDelete: Boolean = false)

fun deleteMark(initial: Boolean = false): KMutableProperty0<Boolean> = DeleteMarkBox(initial)::isInDelete

inline fun String.processMarks(inDeleteMark: KMutableProperty0<Boolean>, defalutValue: () -> String = { "" }): String {
    return when {
        inDeleteMark.get() -> {
            if (trim() == REMOVE_END) {
                inDeleteMark.set(false)
            }
            defalutValue()
        }
    
        trim() == REMOVE_START -> {
            inDeleteMark.set(true)
            defalutValue()
        }
        
        else -> if (REMOVE in this) defalutValue() else removePrefix(PREFIX_RELEACE)
    }
}