
#ifdef _smol_cpp_ // we want to prevent multiple inclusions of this

// TODO; heeeyyyy - can we/I do a version check here?
#	if _smol_cpp_ != %{smol_load.c.hashCode()}
#		error oops! version mistmatch!
#	endif

#else // _smol_cpp_

#	define _smol_cpp_ %{smol_load.c.hashCode()}
#	include <assert.h>
#	include <stdlib.h>
#	include <string.h>

smol_cpp void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames)
{
	const size_t prefix_len = strlen(prefix);

	size_t buffer_size = 0;
	char* buffer_data = nullptr;

	// where in the blob are we
	size_t offset = 0;

	for (size_t i = 0; i < count; ++i)
	{
		// determine the length of the string
		const auto length = strlen(allnames + offset);

		// (maybe) expand the buffer
		const auto needed = length + 1 + prefix_len;
		if (needed > buffer_size)
		{
			// re-allocate the buffer
			buffer_data = reinterpret_cast<char*>(realloc(buffer_data, buffer_size = needed));

			// (re)copy the prefix
			memcpy(buffer_data, prefix, prefix_len);
		}

		// copy the name
		memcpy(buffer_data + prefix_len, allnames + offset, length);

		// put the nil character
		buffer_data[prefix_len + length] = '\0';

		// get the function
		ptr[i] = callback(userdata, buffer_data);

		// advance
		offset += 1 + length;
	}

	assert(buffer_size);
	free(buffer_data);
}

#endif // _smol_cpp_
