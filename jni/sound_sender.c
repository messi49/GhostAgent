#include <jni.h>
#include <android/log.h>

#include <stdio.h>
#include <stdlib.h>

#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>

#define LOG_TAG	"ghostagent"
#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

//Global variable
int sock1, sock2;

// Convert Endian
int convertEndian(void *input, size_t s);

JNIEXPORT void JNICALL Java_com_ghostagent_SoundManagementNative_connectServer(JNIEnv * env, jobject obj, jstring address, jint port_number){
	int sock;
	struct sockaddr_in server;
	const char *address_number = (*env)->GetStringUTFChars(env, address, 0);

	/* create socket */
	sock = socket(AF_INET, SOCK_STREAM, 0);

	/* Preparation of the structure for the specified destination */
	server.sin_family = AF_INET;
	// default: 12335
	server.sin_port = htons(port_number);
	server.sin_addr.s_addr = inet_addr(address_number);

	/* connect to server */
	connect(sock, (struct sockaddr *)&server, sizeof(server));

	if(port_number == 12345)
		sock1 = sock;
	else if(port_number == 30003)
		sock2 = sock;
}

JNIEXPORT void JNICALL Java_com_ghostagent_SoundManagementNative_closeConnect(JNIEnv * env){
	if(close(sock1)<0){
		printf("error_socket\n");
	}
}


JNIEXPORT void JNICALL Java_com_ghostagent_SoundManagementNative_sendSoundData(JNIEnv * env,jobject thiz, jbyteArray data, jint fileSize){
	jbyte* data_array = (jbyte*)(*env)->GetPrimitiveArrayCritical(env, data, NULL);

	__android_log_print(ANDROID_LOG_DEBUG,"Native","size: %d",data_array[fileSize-1]);

	// send file size
	send(sock1, &fileSize, sizeof(fileSize), 0);

	// little endian to big endian
	//convertEndian(data_array, fileSize);

	// send sound data
	send(sock1, data_array, fileSize, 0);

	(*env)->ReleasePrimitiveArrayCritical(env, data, data_array, 0);

}

JNIEXPORT void JNICALL Java_com_ghostagent_SoundManagementNative_sendCommand(JNIEnv * env, jobject obj, jint val, jint kind ){
	struct sockaddr_in server;
	char buf[2];

	buf[0] = (int)val;
	buf[1] = (int)kind;

	// send file size
	send(sock2, buf, sizeof(buf), 0);

}

int convertEndian(void *input, size_t s){
	int i;   // counter
	unsigned char *temp;   // temp

	if((temp = (char *)calloc( s, sizeof(unsigned char))) == NULL){
		return 0;   // error
	}

	for(i=0; i<s; i++){   // save input to temp
		temp[i] = ((unsigned char *)input)[i];
	}

	for(i=1; i<=s; i++){   // reverse
		((unsigned char *)input)[i-1] = temp[s-i];
	}

	free(temp);   // free

	return 1;   // finish
}

