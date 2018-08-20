
#pragma once

#include <stdint.h>
#ifdef tin_c
// i need some extra headers for the body
#include <assert.h>
#include <memory.h>
#include <string.h>
#include <tinfl.h>
#endif // tin_c

// simply all-the-things
#ifdef tin_all
#TIN#
#endif // tin_all

#ifdef __cplusplus
#include <functional>
void tin(const char* filename, std::function<void(const char*, const size_t, const void*)> callback);
extern "C" {
#endif

void tin(const char*, void*, void(*)(void*, const char*, const size_t, const void*));
extern const size_t tin_count;
size_t tin_loaded(void);

// predefined loading thing
#ifdef tin_c
const size_t tin_count = #LEN#;
static size_t _tin_loaded = 0;
size_t tin_loaded(void) { return _tin_loaded; }
void tin(const char* filename, void* userdata, void(*callback)(void*, const char*, const size_t, const void*))
{
	size_t size;
	#define TIN_BEGIN(PATH, SIZE, SPAN) \
		if (!filename || 0 == strcmp(PATH, filename)) \
		{ \
			static void* loaded = NULL; \
			if (!loaded) \
			{ \
				const static uint8_t zipped[] = {

	#define TIN_CLOSE(PATH, SIZE, SPAN) \
				}; \
				loaded = tinfl_decompress_mem_to_heap( \
					zipped, SPAN, \
					&size, \
					0 \
				); \
				assert(NULL != loaded); \
				assert(SIZE == size); \
				++_tin_loaded; \
			} \
			callback(userdata, PATH, SIZE, loaded); \
		}
#TIN#
}
#endif // tin_c
#ifdef __cplusplus
}
inline void tin(const char* filename, std::function<void(const char*, const size_t, const void*)> callback)
{
	tin(filename, reinterpret_cast<void*>(&callback), [](void* userdata, const char* filename, const size_t size, const void* data)
	{
		(*reinterpret_cast<std::function<void(const char*, const size_t, const void*)>*>(userdata))(filename, size, data);
	});
}
#endif
