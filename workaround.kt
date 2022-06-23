fun main() {
    var data = 170000000.toString(16).toByteArray()
    var packet_Name = "transmitter frequency configuration"
    var packet_Header  = 0xaa55.toByte()
    var packet_ID  = ""
    var packet_Response = "0000"
    var data_Length = "0008"
    var packet_data_field = data
    if (packet_Name == "transmitter frequency configuration"){
        packet_ID = "5200"
    }
    println("packet header :${packet_Header}")
}
