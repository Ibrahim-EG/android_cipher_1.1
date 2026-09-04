#include <jni.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

/* ---------- SHA-256 ---------- */
typedef struct {
    uint32_t state[8];
    uint64_t bitlen;
    uint8_t buf[64];
    uint32_t buflen;
} sha256_t;

static const uint32_t SHA256_K[64] = {
    0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
    0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
    0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
    0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
    0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
    0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
    0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
    0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
};

#define ROTR(x,n) (((x) >> (n)) | ((x) << (32 - (n))))

static void sha256_init(sha256_t *c) {
    c->state[0]=0x6a09e667; c->state[1]=0xbb67ae85; c->state[2]=0x3c6ef372; c->state[3]=0xa54ff53a;
    c->state[4]=0x510e527f; c->state[5]=0x9b05688c; c->state[6]=0x1f83d9ab; c->state[7]=0x5be0cd19;
    c->bitlen=0; c->buflen=0;
}

static void sha256_block(sha256_t *c, const uint8_t *p) {
    uint32_t w[64];
    uint32_t a,b,cc,d,e,f,g,h,t1,t2;
    int i;
    for (i=0;i<16;i++)
        w[i] = ((uint32_t)p[i*4]<<24)|((uint32_t)p[i*4+1]<<16)|((uint32_t)p[i*4+2]<<8)|((uint32_t)p[i*4+3]);
    for (i=16;i<64;i++) {
        uint32_t s0 = ROTR(w[i-15],7)^ROTR(w[i-15],18)^(w[i-15]>>3);
        uint32_t s1 = ROTR(w[i-2],17)^ROTR(w[i-2],19)^(w[i-2]>>10);
        w[i] = w[i-16]+s0+w[i-7]+s1;
    }
    a=c->state[0]; b=c->state[1]; cc=c->state[2]; d=c->state[3];
    e=c->state[4]; f=c->state[5]; g=c->state[6]; h=c->state[7];
    for (i=0;i<64;i++) {
        uint32_t S1 = ROTR(e,6)^ROTR(e,11)^ROTR(e,25);
        uint32_t ch = (e&f)^((~e)&g);
        t1 = h+S1+ch+SHA256_K[i]+w[i];
        uint32_t S0 = ROTR(a,2)^ROTR(a,13)^ROTR(a,22);
        uint32_t maj = (a&b)^(a&cc)^(b&cc);
        t2 = S0+maj;
        h=g; g=f; f=e; e=d+t1; d=cc; cc=b; b=a; a=t1+t2;
    }
    c->state[0]+=a; c->state[1]+=b; c->state[2]+=cc; c->state[3]+=d;
    c->state[4]+=e; c->state[5]+=f; c->state[6]+=g; c->state[7]+=h;
}

static void sha256_update(sha256_t *c, const uint8_t *data, size_t len) {
    size_t i;
    for (i=0;i<len;i++) {
        c->buf[c->buflen++]=data[i];
        if (c->buflen==64) { sha256_block(c,c->buf); c->bitlen+=512; c->buflen=0; }
    }
}

static void sha256_final(sha256_t *c, uint8_t *out) {
    uint64_t bitlen = c->bitlen + (uint64_t)c->buflen * 8;
    int i;
    c->buf[c->buflen++] = 0x80;
    if (c->buflen > 56) {
        while (c->buflen < 64) c->buf[c->buflen++] = 0;
        sha256_block(c, c->buf);
        c->buflen = 0;
    }
    while (c->buflen < 56) c->buf[c->buflen++] = 0;
    for (i=7;i>=0;i--) c->buf[c->buflen++] = (uint8_t)(bitlen >> (i*8));
    sha256_block(c, c->buf);
    for (i=0;i<8;i++) {
        out[i*4]   = (uint8_t)(c->state[i] >> 24);
        out[i*4+1] = (uint8_t)(c->state[i] >> 16);
        out[i*4+2] = (uint8_t)(c->state[i] >> 8);
        out[i*4+3] = (uint8_t)(c->state[i]);
    }
}

static void hmac_sha256(const uint8_t *key, size_t keylen, const uint8_t *msg, size_t msglen, uint8_t *out) {
    uint8_t k[64];
    uint8_t pad[64];
    uint8_t inner[32];
    sha256_t c;
    size_t i;
    memset(k,0,64);
    if (keylen > 64) { sha256_init(&c); sha256_update(&c,key,keylen); sha256_final(&c,k); }
    else memcpy(k,key,keylen);
    sha256_init(&c);
    for (i=0;i<64;i++) pad[i]=k[i]^0x36;
    sha256_update(&c,pad,64);
    sha256_update(&c,msg,msglen);
    sha256_final(&c,inner);
    sha256_init(&c);
    for (i=0;i<64;i++) pad[i]=k[i]^0x5c;
    sha256_update(&c,pad,64);
    sha256_update(&c,inner,32);
    sha256_final(&c,out);
}

static void pbkdf2_sha256(const uint8_t *pw, size_t pwlen, const uint8_t *salt, size_t saltlen,
                          uint32_t iterations, uint8_t *out, size_t dklen) {
    uint32_t blocks = (uint32_t)((dklen + 31) / 32);
    uint8_t *U = (uint8_t*)malloc(32);
    uint8_t *T = (uint8_t*)malloc(32);
    uint8_t *S = (uint8_t*)malloc(saltlen + 4);
    uint32_t i; int k;
    if (!U || !T || !S) { free(U); free(T); free(S); return; }
    for (i = 1; i <= blocks; i++) {
        memcpy(S, salt, saltlen);
        S[saltlen]   = (uint8_t)(i >> 24);
        S[saltlen+1] = (uint8_t)(i >> 16);
        S[saltlen+2] = (uint8_t)(i >> 8);
        S[saltlen+3] = (uint8_t)(i);
        hmac_sha256(pw, pwlen, S, saltlen+4, U);
        memcpy(T, U, 32);
        for (uint32_t j = 1; j < iterations; j++) {
            hmac_sha256(pw, pwlen, U, 32, U);
            for (k = 0; k < 32; k++) T[k] ^= U[k];
        }
        size_t copy = dklen - (size_t)(i-1)*32; if (copy > 32) copy = 32;
        memcpy(out + (size_t)(i-1)*32, T, copy);
    }
    free(U); free(T); free(S);
}

/* ---------- Little-endian helpers ---------- */
static uint32_t le32(const uint8_t *p) { return (uint32_t)p[0]|((uint32_t)p[1]<<8)|((uint32_t)p[2]<<16)|((uint32_t)p[3]<<24); }
static void st32(uint8_t *p, uint32_t v) { p[0]=(uint8_t)v; p[1]=(uint8_t)(v>>8); p[2]=(uint8_t)(v>>16); p[3]=(uint8_t)(v>>24); }
static uint64_t le64(const uint8_t *p) { return (uint64_t)le32(p) | ((uint64_t)le32(p+4) << 32); }

/* ---------- Salsa20/8 ---------- */
static void salsa20_8(uint32_t B[16]) {
    uint32_t x[16];
    int i;
    memcpy(x, B, 64);
    for (i = 0; i < 8; i += 2) {
#define R(a,b) (((a)<<(b))|((a)>>(32-(b))))
        x[ 4]^=R(x[ 0]+x[12], 7); x[ 8]^=R(x[ 4]+x[ 0], 9); x[12]^=R(x[ 8]+x[ 4],13); x[ 0]^=R(x[12]+x[ 8],18);
        x[ 9]^=R(x[ 5]+x[ 1], 7); x[13]^=R(x[ 9]+x[ 5], 9); x[ 1]^=R(x[13]+x[ 9],13); x[ 5]^=R(x[ 1]+x[13],18);
        x[14]^=R(x[10]+x[ 6], 7); x[ 2]^=R(x[14]+x[10], 9); x[ 6]^=R(x[ 2]+x[14],13); x[10]^=R(x[ 6]+x[ 2],18);
        x[ 3]^=R(x[15]+x[11], 7); x[ 7]^=R(x[ 3]+x[15], 9); x[11]^=R(x[ 7]+x[ 3],13); x[15]^=R(x[11]+x[ 7],18);
        x[ 1]^=R(x[ 0]+x[ 3], 7); x[ 2]^=R(x[ 1]+x[ 0], 9); x[ 3]^=R(x[ 2]+x[ 1],13); x[ 0]^=R(x[ 3]+x[ 2],18);
        x[ 6]^=R(x[ 5]+x[ 4], 7); x[ 7]^=R(x[ 6]+x[ 5], 9); x[ 4]^=R(x[ 7]+x[ 6],13); x[ 5]^=R(x[ 4]+x[ 7],18);
        x[11]^=R(x[10]+x[ 9], 7); x[ 8]^=R(x[11]+x[10], 9); x[ 9]^=R(x[ 8]+x[11],13); x[10]^=R(x[ 9]+x[ 8],18);
        x[12]^=R(x[15]+x[14], 7); x[13]^=R(x[12]+x[15], 9); x[14]^=R(x[13]+x[12],13); x[15]^=R(x[14]+x[13],18);
#undef R
    }
    for (i = 0; i < 16; i++) B[i] += x[i];
}

/* ---------- BlockMix ---------- */
static void blockmix(const uint8_t *B, uint8_t *Y, uint32_t r) {
    uint8_t X[64], T[64];
    uint32_t W[16];
    uint32_t i; int j;
    memcpy(X, B + ((size_t)(2*r)-1)*64, 64);
    for (i = 0; i < 2*r; i++) {
        for (j = 0; j < 64; j++) T[j] = X[j] ^ B[(size_t)i*64+j];
        for (j = 0; j < 16; j++) W[j] = le32(T+4*j);
        salsa20_8(W);
        for (j = 0; j < 16; j++) st32(X+4*j, W[j]);
        size_t pos = (i % 2 == 0) ? (i/2) : (i/2 + r);
        memcpy(Y + pos*64, X, 64);
    }
}

/* ---------- ROMix (uses NATIVE memory via malloc, like Termux) ---------- */
static int romix(uint8_t *B, uint32_t r, uint64_t N) {
    size_t blen = (size_t)128 * r;
    uint8_t *V = (uint8_t*)malloc((size_t)N * blen);
    uint8_t *T = (uint8_t*)malloc(blen);
    uint64_t i; size_t k;
    if (!V || !T) { free(V); free(T); return -1; }
    for (i = 0; i < N; i++) {
        memcpy(V + i*blen, B, blen);
        blockmix(B, T, r);
        memcpy(B, T, blen);
    }
    for (i = 0; i < N; i++) {
        uint64_t j = le64(B + ((size_t)(2*r)-1)*64) & (N - 1);
        for (k = 0; k < blen; k++) B[k] ^= V[j*blen + k];
        blockmix(B, T, r);
        memcpy(B, T, blen);
    }
    free(V); free(T);
    return 0;
}

/* ---------- Scrypt (RFC 7914) ---------- */
static int scrypt(const uint8_t *pw, size_t pwlen, const uint8_t *salt, size_t saltlen,
                  uint64_t N, uint32_t r, uint32_t p, uint8_t *dk, size_t dklen) {
    size_t blen = 128 * (size_t)r;
    uint8_t *B = (uint8_t*)malloc(blen * p);
    uint32_t i;
    if (!B) return -1;
    pbkdf2_sha256(pw, pwlen, salt, saltlen, 1, B, blen * p);
    for (i = 0; i < p; i++) {
        if (romix(B + i*blen, r, N) != 0) { free(B); return -1; }
    }
    pbkdf2_sha256(pw, pwlen, B, blen * p, 1, dk, dklen);
    free(B);
    return 0;
}

/* ---------- JNI bridge ---------- */
JNIEXPORT jbyteArray JNICALL
Java_com_example_premiumcipher_NativeScrypt_scryptNative(JNIEnv *env, jclass cls,
        jbyteArray jpass, jbyteArray jsalt, jlong N, jint r, jint p, jint dklen) {
    jsize passlen = (*env)->GetArrayLength(env, jpass);
    jsize saltlen = (*env)->GetArrayLength(env, jsalt);
    jbyte *pass = (*env)->GetByteArrayElements(env, jpass, NULL);
    jbyte *salt = (*env)->GetByteArrayElements(env, jsalt, NULL);
    uint8_t *out = (uint8_t*)malloc((size_t)dklen);
    jbyteArray result = NULL;
    (void)cls;
    if (out) {
        if (scrypt((const uint8_t*)pass, (size_t)passlen, (const uint8_t*)salt, (size_t)saltlen,
                   (uint64_t)N, (uint32_t)r, (uint32_t)p, out, (size_t)dklen) == 0) {
            result = (*env)->NewByteArray(env, dklen);
            if (result) (*env)->SetByteArrayRegion(env, result, 0, dklen, (const jbyte*)out);
        }
        free(out);
    }
    (*env)->ReleaseByteArrayElements(env, jpass, pass, JNI_ABORT);
    (*env)->ReleaseByteArrayElements(env, jsalt, salt, JNI_ABORT);
    return result;
}
