/*
 * chemistry_InChIJNI.c
 *
 *  Created on: Oct 8, 2014
 *      Author: clarkm
 */

#include <jni.h>

/*
 * Class:     chemistry_InChIJNI
 * Method:    getInChIKey
 * Signature: ([Ljava/lang/String;)V
 *
 * @author Matthew Clark
 * @date   October 10, 2014
 */
JNIEXPORT void JNICALL Java_inchi_InChIJNI_runInchi
  (JNIEnv *env, jobject obj, jobjectArray stringArray) {

	int argc = (*env)->GetArrayLength(env, stringArray);
	char * argv[argc];
	/*
	 * process the C strings into java strings.
	 */
	int i;
	for (i = 0; i < argc; i++) {
		jstring argument = (*env)->GetObjectArrayElement(env, stringArray, i);
		argv[i] = (*env)->GetStringUTFChars(env, argument, 0);

	}

	/*
	 * redirect stderr to null for this program because it is
	 * annoyingly chatty
	 */
    int    fd;
    fpos_t pos;
    fflush(stderr);
    fgetpos(stderr, &pos);
    fd = dup(fileno(stderr));
   	freopen ("/dev/null","w",stderr);

	main(argc, argv);

	/*
	 * set stderr back to normal
	 */
    fflush(stderr);
    dup2(fd, fileno(stderr));
    close(fd);
    clearerr(stderr);
    fsetpos(stderr, &pos);


    /*
     * release memory for the argument strings
     */
	for (i = 0; i < argc; i++) {
		jstring argument = (*env)->GetObjectArrayElement(env, stringArray, i);
		(*env)->ReleaseStringUTFChars(env, argument, argv[i]);
	}

    return;

}
