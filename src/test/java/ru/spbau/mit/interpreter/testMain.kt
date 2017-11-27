package ru.spbau.mit.interpreter

import org.junit.After
import org.junit.Before
import org.junit.Test
import ru.spbau.mit.main
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.assertEquals

class TestMain {
    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()

    @Before
    fun setUpStreams() {
        System.setOut( PrintStream( outContent ) )
        System.setErr( PrintStream( errContent ) )
    }

    @After
    fun cleanUpStreams() {
        assertEquals( 0, errContent.size(), errContent.toString() )
        outContent.reset()
        errContent.reset()
        System.setErr( null )
        System.setOut( null )
    }

    private fun assertPrints( text: String ) {
        assert( outContent.toString() == text )
    }

    @Test
    fun testSimple() {
        val tempFile = createTempFile()
        tempFile.writeText( "println(179)" )
        main( Array( 2, { tempFile.absolutePath } ) )
        assertPrints( "179\n" )
    }
}
