export function useAuthErrorRedirect(error) {
  const router = useRouter()
  let redirectTimeout = null

  watch(error, (newError) => {
    if (redirectTimeout) {
      clearTimeout(redirectTimeout)
      redirectTimeout = null
    }

    if (newError && typeof newError === 'string' && newError.toLowerCase().includes('not authenticated')) {
      redirectTimeout = setTimeout(() => {
        router.push('/auth/login')
      }, 3000)
    }
  }, { immediate: true })

  onUnmounted(() => {
    if (redirectTimeout) {
      clearTimeout(redirectTimeout)
    }
  })
}
