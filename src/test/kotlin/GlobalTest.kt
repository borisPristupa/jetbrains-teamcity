import com.boris.internship.migration.Migrator
import com.boris.internship.migration.NewStorage
import com.boris.internship.migration.OldStorage
import org.junit.Assert.assertEquals
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class GlobalTest {
    @Test
    fun `test 1 - copy files from old storage to new`() {
        val olds = Migrator.fileAmount(OldStorage)
        val news = Migrator.fileAmount(NewStorage)
        Migrator.copy(OldStorage, NewStorage)
        assertEquals(olds, Migrator.fileAmount(NewStorage) - news)
    }

    @Test
    fun `test 2 - clear new storage`() {
        Migrator.clear(NewStorage)
        assertEquals(0, Migrator.fileAmount(NewStorage))
    }

    @Test
    fun `test 3 - migrate files from old storage to new`() {
        val olds = Migrator.fileAmount(OldStorage)
        val news = Migrator.fileAmount(NewStorage)
        Migrator.migrate(OldStorage, NewStorage)
        assertEquals(olds, Migrator.fileAmount(NewStorage) - news)
        assertEquals(0, Migrator.fileAmount(OldStorage))
    }
}