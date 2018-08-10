if (!filename || 0 == strcmp(NAME, filename))
{
	static void* loaded = NULL;
	if (!loaded)
	{
		const static uint8_t zipped[] = {
      #TIN#
		};
		loaded = tinfl_decompress_mem_to_heap(
			zipped, SPAN,
			&size,
			0
		);
		assert(NULL != loaded);
		assert(SIZE == size);
		++_tin_loaded;
	}
	callback(userdata, NAME, size, loaded);
}
